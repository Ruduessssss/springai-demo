package com.git.hui.ai.app.mvc;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

/**
 * @author YiHui
 * @date 2025/8/26
 */
@RestController
public class ChatController {
    private final ChatModel chatModel;

    public ChatController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * OpenAI 兼容模型的流式推理
     *
     * @param msg
     * @return
     */
    @GetMapping(path = "aliChatWithThinking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatV5(String msg) {
        SseEmitter sseEmitter = new SseEmitter();
        Flux<ChatResponse> res = chatModel.stream(new Prompt(new UserMessage(msg)));
        StringBuilder content = new StringBuilder();
        StringBuilder reason = new StringBuilder();
        res.doOnComplete(() -> {
                    sseEmitter.complete();
                    System.out.println("思考过程:" + reason);
                    System.out.println("结果:" + content);
                })
                .subscribe(txt -> {
                    Generation generation = txt.getResult();

                    var r = generation.getOutput().getMetadata().get("reasoningContent");
                    if (r != null) {
                        reason.append(r);
                    }

                    var t = generation.getOutput().getText();
                    if (t != null) {
                        content.append(t);
                    }
                    try {
                        sseEmitter.send("思考:" + reason + "===>\n<br/>\n==>" + content);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return sseEmitter;
    }

    /**
     * OpenAI 兼容模型
     *
     * @param msg
     * @return
     */
    @GetMapping(path = {"openaiChat", "zhipuChat"})
    public Map zhipuChat(String msg) {
        ChatClient client = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
        Flux<ChatResponse> res = client.prompt(new Prompt(msg)).stream().chatResponse();
        StringBuilder content = new StringBuilder();
        StringBuilder reason = new StringBuilder();
        ChatResponse response = res.doOnComplete(() -> {
            System.out.println("思考过程:" + reason);
            System.out.println("结果:" + content);
        }).doOnNext(txt -> {
            Generation generation = txt.getResult();
            var r = generation.getOutput().getMetadata().get("reasoningContent");
            if (r != null) {
                reason.append(r);
                System.out.println("思考:" + r);
            }
            var t = generation.getOutput().getText();
            if (t != null) {
                content.append(t);
                System.out.println("结果:" + t);
            }
        }).blockLast();
        // token使用
        var usage = response.getMetadata().getUsage();
        // 构建完成的返回结果
        return Map.of("思考过程", reason, "结果", content, "token消耗", usage);
    }

    @GetMapping(path = {"openaiChatV2", "zhipuChatV2"})
    public Map zhipuChatV2(String msg) {
        ChatClient client = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        ChatResponse response = client.prompt(new Prompt(msg)).call().chatResponse();
        var reason = response.getResult().getOutput().getMetadata().get("reasoningContent");
        var content = response.getResult().getOutput().getText();

        // token使用
        var usage = response.getMetadata().getUsage();
        // 构建完成的返回结果
        return Map.of("思考过程", reason == null ? "" : reason, "结果", content, "token消耗", usage);
    }
}

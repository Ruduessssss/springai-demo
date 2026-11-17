package com.git.hui.springai.mvc;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author YiHui
 * @date 2025/8/26
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    /**
     * 手动构建的 OpenAI 兼容模型
     */
    private final ChatModel manualModel;


    public ChatController(ChatModel chatModel, Environment environment) {
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();


        // 通过手动的方式，注册 手动构建的 OpenAI 兼容模型
        OpenAiApi openAiApi = OpenAiApi.builder().apiKey(environment.getRequiredProperty("spring.ai.openai.api-key"))
                .baseUrl(environment.getRequiredProperty("spring.ai.openai.base-url"))
                .completionsPath("/chat/completions")
                .build();
        manualModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(environment.getRequiredProperty("spring.ai.openai.chat.options.model")).build())
                .build();

    }

    @GetMapping(path = "chat")
    public String chat(String msg) {
        return chatClient.prompt(msg).call().content();
    }


    /**
     * 阿里百炼模型
     *
     * @param msg
     * @return
     */
    @GetMapping(path = "aliChat")
    public String aliChat(String msg) {
        return manualModel.call(new UserMessage(msg));
    }
}

package com.git.hui.offer.model;

import com.git.hui.offer.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Consumer;

/**
 * 手动调用 OpenAI 兼容接口的 ChatModel 示例
 *
 * @author YiHui
 * @date 2025/7/21
 */
@Component
public class SparkLiteModel implements ChatModel {
    private RestClient restClient;

    @Value("${MODEL_API_KEY}")
    private String apiKey;
    @Value("${MODEL_NAME}")
    private String model;
    @Value("${MODEL_BASE_URL}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        Consumer<HttpHeaders> authHeaders = (h) -> {
            h.setBearerAuth(apiKey);
            h.setContentType(MediaType.APPLICATION_JSON);
        };

        this.restClient = RestClient.builder().baseUrl(baseUrl).defaultHeaders(authHeaders).build();
    }

    /**
     * 配置默认的查询条件
     *
     * @return
     */
    @Override
    public ChatOptions getDefaultOptions() {
        return ChatOptions.builder()
                .model(model)
                .build();
    }

    /**
     * 这里实现了一个基本的模型调用逻辑
     *  todo: function tool的能力支持
     *  todo: 多轮对话，上下文的支持
     *
     * @param prompt
     * @return
     */
    @Override
    public ChatResponse call(Prompt prompt) {
        Long reqTime = System.currentTimeMillis();
        String model = (prompt.getOptions() == null || prompt.getOptions().getModel() == null) ? this.model : prompt.getOptions().getModel();
        String res = restClient.post().uri("/chat/completions").body(POJOConvert.toReq(prompt, model)).retrieve().body(String.class);

        SparkPOJO.ChatCompletionChunk chatCompletionChunk = JsonUtil.fromStr(res, SparkPOJO.ChatCompletionChunk.class);
        List<Generation> generations = POJOConvert.generationList(chatCompletionChunk);
        ChatResponse response = new ChatResponse(generations, POJOConvert.from(reqTime, model, chatCompletionChunk));
        return response;
    }
}

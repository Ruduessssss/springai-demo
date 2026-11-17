package com.git.hui.springai;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

class ChatClientBoundaryTest {

    @Test
    void observeCallBoundary() {
        AtomicInteger calls = new AtomicInteger();

        // 替代真实模型：每次被调用时计数，不发送网络请求。
        ChatModel fakeModel = new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                calls.incrementAndGet();
                return new ChatResponse(
                        List.of(new Generation(
                                new AssistantMessage("测试回复")
                        ))
                );
            }
        };

        ChatClient client = ChatClient.builder(fakeModel).build();

        var responseSpec = client.prompt().user("你好").call();
        int afterCall = calls.get();

        String text = responseSpec.content();
        int afterContent = calls.get();

        System.out.println("afterCall = " + afterCall);
        System.out.println("afterContent = " + afterContent);
        System.out.println("text = " + text);
    }
}

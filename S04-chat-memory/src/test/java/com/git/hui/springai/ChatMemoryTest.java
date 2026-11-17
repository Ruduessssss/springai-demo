package com.git.hui.springai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatMemoryTest {


    @Test
    void testMemoryGroup()
    {

        MessageWindowChatMemory memory = MessageWindowChatMemory.builder().build();

        List<List<String>> receivedUserMessages = new ArrayList<>();

        ChatModel fakeModel = new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                List<String> list = prompt.getInstructions().stream().filter(UserMessage.class::isInstance)
                        .map(message -> {
                            return message.getText();
                        })
                        .toList();
                receivedUserMessages.add(list);
                return new ChatResponse(List.of(new Generation(new AssistantMessage("收到了"))));
            }
        };

        ChatClient chatClient = ChatClient.builder(fakeModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();

        ask(chatClient,"A","代号是青竹");
        ask(chatClient,"A","代号是什么");
        ask(chatClient,"B","代号是什么");

        assertEquals(List.of("代号是青竹"),receivedUserMessages.get(0));
        assertEquals(List.of("代号是青竹","代号是什么"),receivedUserMessages.get(1));
        assertEquals(List.of("代号是什么"),receivedUserMessages.get(2));

    }

    @Test
    void memoryObjectsCanShareARepository() {
        var repository = new InMemoryChatMemoryRepository();

        var firstMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .build();

        var secondMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .build();

        firstMemory.add("A", new UserMessage("代号是青竹"));

        // 两个记忆对象使用同一个 Repository。
        assertEquals(
                "代号是青竹",
                secondMemory.get("A").get(0).getText()
        );

        var freshMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();

        // 会话 ID 相同，但使用了另一份新的内存存储。
        assertTrue(freshMemory.get("A").isEmpty());
    }



    void ask(ChatClient chatClient,String id,String text)
    {
        chatClient.prompt().user(text)
                .advisors(
                        advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, id))
                .call()
                .content();
    }
}

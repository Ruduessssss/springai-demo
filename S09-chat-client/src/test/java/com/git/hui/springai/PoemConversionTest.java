package com.git.hui.springai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.boot.json.JsonParseException;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PoemConversionTest {

    public record Poem(String title, String content) {
    }

    @Test
    void convertPoem() {
        var converter = new BeanOutputConverter<>(Poem.class);

        // 第一端：取得“应该怎样输出”的说明。
        String formatInstructions = converter.getFormat();
        System.out.println(formatInstructions);

        // 用固定文本代替模型回复，单独验证转换。
        String reply = "标题是月夜，正文是示例诗句。";

        // 第二端：把回复文本解析成 Java 对象。
        //Poem poem = converter.convert(reply);
        assertThrows(RuntimeException.class,()->{
            converter.convert(reply);
        });
//        assertEquals("月夜", poem.title());
//        assertEquals("示例诗句", poem.content());


    }

    @Test
    void entityAddsFormatAndConvert()
    {
        AtomicReference<Prompt> capturedPrompt = new AtomicReference<>();

        ChatModel fakeModel = new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                capturedPrompt.set(prompt);
                String reply = """
                        {
                          "title": "月夜",
                          "content": "示例诗句"
                        }
                        """;
                return new ChatResponse(List.of(new Generation(new AssistantMessage(reply))));
            }
        };

        ChatClient chatClient = ChatClient.builder(fakeModel).build();
        Poem poem = chatClient.prompt()
                .user("写一首月亮的诗")
                .call()
                .entity(Poem.class);

        assertNotNull(poem);
        assertEquals("月夜",poem.title);
        assertEquals("示例诗句", poem.content());

        assertNotNull(capturedPrompt.get());
        capturedPrompt.get().getInstructions()
                .forEach(message -> System.out.println(message.getText()));


    }
}

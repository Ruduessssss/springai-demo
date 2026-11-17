package com.git.hui.offer;

import org.junit.jupiter.api.Test;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolCallbackBindingTest {

    public static class CounterTool {
        int calls = 0;

        @Tool(description = "记录一次执行，并返回累计执行次数")
        public int recordCall() {
            calls++;
            return calls;
        }
    }


    public static class CityTool {
        String receivedCity;
        int calls = 0;

        @Tool(description = "接收城市名并记录")
        public String recordCity(String city) {
            receivedCity = city;
            calls++;
            return "已记录城市：" + city;
        }
    }

    @Test
    void selectCallbackByToolName() {
        CounterTool counter = new CounterTool();
        CityTool cityTool = new CityTool();

        ToolCallback[] callbacks = ToolCallbacks.from(
                counter,
                cityTool
        );

        // 模拟收到一条工具调用请求。
        String requestedName = "unknown";
        String argumentsJson = "{\"city\":\"北京\"}";

        ToolCallback selected = null;

        for (ToolCallback callback : callbacks) {
            String name = callback.getToolDefinition().name();

            if (name.equals(requestedName)) {
                selected = callback;
                break;
            }
        }

        if (selected == null) {
            throw new IllegalArgumentException(
                    "找不到工具：" + requestedName
            );
        }

        selected.call(argumentsJson);

        assertEquals("北京", cityTool.receivedCity);
        assertEquals(1, cityTool.calls);
        assertEquals(0, counter.calls);
    }



    @Test
    void callbackConvertsJsonIntoMethodArgument() {
        CityTool target = new CityTool();

        ToolCallback[] callbacks = ToolCallbacks.from(target);
        ToolCallback callback = callbacks[0];

        System.out.println(
                "输入结构：" + callback.getToolDefinition().inputSchema()
        );

        // 此时只是准备回调，没有执行工具。
        assertEquals(0, target.calls);

        // 传入的是工具参数 JSON，不是 Java 对象。
        String result = callback.call("{\"city\":\"北京\"}");

        assertEquals(1, target.calls);
        assertEquals("北京", target.receivedCity);

        System.out.println("工具执行结果：" + result);
    }


    @Test
    void callbackUsesTheProvidedObject() {
        CounterTool first = new CounterTool();
        CounterTool second = new CounterTool();

        ToolCallback[] callbacks = ToolCallbacks.from(first);

        var definition = callbacks[0].getToolDefinition();

        System.out.println("工具名：" + definition.name());
        System.out.println("说明：" + definition.description());
        System.out.println("输入结构：" + definition.inputSchema());

        assertEquals(0, first.calls);

        // 构造回调，不应该执行工具方法。
        assertEquals(1, callbacks.length);
        assertEquals(0, first.calls);
        assertEquals(0, second.calls);

        System.out.println(
                "回调实现类：" + callbacks[0].getClass().getName()
        );

        // 直接调用工具回调；{} 表示没有业务参数。
        callbacks[0].call("{}");

        // 被执行的是 first 对象上的方法。
        assertEquals(1, first.calls);
        assertEquals(0, second.calls);
    }
}

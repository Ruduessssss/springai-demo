package com.git.hui.offer;

import com.git.hui.offer.mvc.ChatController.AreaReq;
import com.git.hui.offer.mvc.ChatController.NowService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import static org.junit.jupiter.api.Assertions.assertFalse;

class FunctionToolCallbackTest {

    @Test
    void findFunctionByBeanName() {
        try (var context =
                     new org.springframework.context.annotation
                             .AnnotationConfigApplicationContext(
                             com.git.hui.offer.config.ToolConfig.class
                     )) {

            Object found = context.getBean("nowService");

            org.junit.jupiter.api.Assertions.assertInstanceOf(
                    NowService.class,
                    found
            );
        }
    }



    @Test
    void executeFunctionTool() {
        NowService function = new NowService();

        ToolCallback callback =
                FunctionToolCallback.builder("nowDateByArea", function)
                        .description("查询指定时区的时间")
                        .inputType(AreaReq.class)
                        .build();

        System.out.println(
                "输入结构：" + callback.getToolDefinition().inputSchema()
        );

        String result = callback.call(
                "{\"zoneId\":\"Asia/Shanghai\"}"
        );

        System.out.println("工具结果：" + result);
        assertFalse(result.isBlank());
    }

    @Test
    void inspectToolRegistrationApi() {
        var type =
                org.springframework.ai.chat.client.ChatClient
                        .ChatClientRequestSpec.class;

        java.util.Arrays.stream(type.getMethods())
                .filter(method ->
                        method.getName().equals("tools")
                                || method.getName().equals("toolNames"))
                .forEach(method ->
                        System.out.println(method.toGenericString()));
    }

}

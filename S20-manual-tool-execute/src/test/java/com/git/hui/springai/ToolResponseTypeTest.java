package com.git.hui.springai;

import com.git.hui.springai.tools.ToolResponseType;
import com.git.hui.springai.tools.WeatherTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ToolResponseTypeTest {

    @Test
    void readResponseTypeLabel() {
        Method method = ReflectionUtils.findMethod(
                WeatherTools.class,
                "queryWeather",
                String.class,
                ToolContext.class
        );

        assertNotNull(method);

        ToolResponseType annotation =
                method.getDeclaredAnnotation(ToolResponseType.class);

        assertNotNull(annotation);

        String label = annotation.value();

        System.out.println("读取到的标签：" + label);
        assertEquals("card", label);
    }
}

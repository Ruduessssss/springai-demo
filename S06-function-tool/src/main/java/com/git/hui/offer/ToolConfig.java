package com.git.hui.offer.config;

import com.git.hui.offer.mvc.ChatController.AreaReq;
import com.git.hui.offer.mvc.ChatController.AreaResp;
import com.git.hui.offer.mvc.ChatController.NowService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class ToolConfig {

    @Bean("nowService")
    @Description("传入时区，返回对应时区的当前时间")
    public Function<AreaReq, AreaResp> nowService() {
        return new NowService();
    }
}

package com.git.hui.ai.app;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

public class FluxBasicsTest {

    @Test
    void observeFailure()
    {
        Flux<String> pieces = Flux.just("甲", "乙").concatWith(Flux.error(
                new IllegalStateException("模拟故障")
        )).onErrorResume(
                error->{
                    return Flux.just("错误回复,内容不完整");
                }
        );

        pieces.subscribe(
                piece->{
                    System.out.println("收到元素:"+piece);
                },
                error->{
                    System.out.println("故障:"+error);
                },
                ()->{
                    System.out.println("序列完成");
                }
        );
    }
}

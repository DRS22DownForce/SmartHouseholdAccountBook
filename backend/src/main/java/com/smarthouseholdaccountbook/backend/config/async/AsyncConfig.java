package com.example.backend.config.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * AIカテゴリー推論のための非同期処理の設定
     * @return AIカテゴリー推論のための非同期処理のExecutor
     */
    @Bean(name = "aiCategoryTaskExecutor")
    public Executor aiCategoryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-category");
        executor.initialize();
        return executor;
    }
}

package com.smarthouseholdaccountbook.backend.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    /**
     * AIカテゴリー推論のチャンク並列処理用 Executor。
     *
     * <p>core/max を 3 に固定し、同時に走る OpenAI リクエスト数を抑える。
     *
     * @return AIカテゴリー推論用の Executor
     */
    @Bean(name = "aiCategoryTaskExecutor")
    public Executor aiCategoryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-category-");
        executor.initialize();
        return executor;
    }
}

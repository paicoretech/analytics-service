package com.paic.nbm.analyticsservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class PcapExecutorConfig {

    @Value("${max.allowed.downloads:2}")
    private int maxAllowedDownloads;

    @Bean(name = "pcapExecutor")
    public ThreadPoolTaskExecutor pcapExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int effectiveMax = Math.max(2, maxAllowedDownloads);

        executor.setCorePoolSize(effectiveMax);
        executor.setMaxPoolSize(effectiveMax);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("PcapDownload-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
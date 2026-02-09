package com.oddfar.campus.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务配置类
 * 启用Spring的定时任务支持，并配置线程池
 * 
 * @author AI Assistant
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * 配置定时任务的线程池
     * 使用线程池可以避免任务阻塞，提高并发性能
     * 
     * @return ThreadPoolTaskScheduler 定时任务线程池
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // 设置线程池大小
        scheduler.setPoolSize(5);
        
        // 设置线程名称前缀
        scheduler.setThreadNamePrefix("scheduled-task-");
        
        // 设置线程池关闭时是否等待所有任务完成
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        // 设置线程池关闭时等待任务完成的最大时间（毫秒）
        scheduler.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        scheduler.initialize();
        
        return scheduler;
    }
}
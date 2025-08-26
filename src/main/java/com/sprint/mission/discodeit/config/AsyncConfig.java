package com.sprint.mission.discodeit.config;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {

    @Bean
    public TaskDecorator mdcAndSecurityContextTaskDecorator() {
        return task -> {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            SecurityContext securityContext = SecurityContextHolder.getContext();

            return () -> {
                try {
                    if (mdc != null) {
                        MDC.setContextMap(mdc);
                    } else {
                        MDC.clear();
                    }
                    SecurityContextHolder.setContext(securityContext);

                    task.run();
                } finally {
                    MDC.clear();
                    SecurityContextHolder.clearContext();
                }
            };
        };
    }

    private ThreadPoolTaskExecutor buildExecutor(int core, int max, int queue, int keepAlive,
        String prefix, TaskDecorator taskDecorator) {

        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();

        exec.setCorePoolSize(core);
        exec.setMaxPoolSize(max);
        exec.setQueueCapacity(queue);
        exec.setKeepAliveSeconds(keepAlive);
        exec.setThreadNamePrefix(prefix + "-");
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        exec.setWaitForTasksToCompleteOnShutdown(true);
        exec.setAwaitTerminationSeconds(20);
        exec.setTaskDecorator(taskDecorator);
        exec.initialize();

        return exec;
    }

    @Bean(name = "fileTaskExecutor")
    public ThreadPoolTaskExecutor fileTaskExecutor(
        @Value("${async.executors.file.core-size:2}") int core,
        @Value("${async.executors.file.max-size:4}") int max,
        @Value("${async.executors.file.queue-capacity:100}") int queue,
        @Value("${async.executors.file.keep-alive-seconds:120}") int keepAlive,
        TaskDecorator mdcAndSecurityContextTaskDecorator
    ) {
        return buildExecutor(core, max, queue, keepAlive, "file-exec",
            mdcAndSecurityContextTaskDecorator);
    }

    @Bean(name = "notificationTaskExecutor")
    public ThreadPoolTaskExecutor notificationTaskExecutor(
        @Value("${async.executors.notification.core-size:2}") int core,
        @Value("${async.executors.notification.max-size:2}") int max,
        @Value("${async.executors.notification.queue-capacity:2}") int queue,
        @Value("${async.executors.notification.keep-alive-seconds:2}") int keepAlive,
        TaskDecorator mdcAndSecurityContextTaskDecorator
    ) {
        return buildExecutor(core, max, queue, keepAlive, "notify-exec",
            mdcAndSecurityContextTaskDecorator);
    }

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor defaultTaskExecutor(
        @Value("${async.executors.default.core-size:2}") int core,
        @Value("${async.executors.default.max-size:4}") int max,
        @Value("${async.executors.default.queue-capacity:100}") int queue,
        @Value("${async.executors.default.keep-alive-seconds:60}") int keepAlive,
        TaskDecorator mdcAndSecurityContextTaskDecorator
    ) {
        return buildExecutor(core, max, queue, keepAlive, "default-exec",
            mdcAndSecurityContextTaskDecorator);
    }
}

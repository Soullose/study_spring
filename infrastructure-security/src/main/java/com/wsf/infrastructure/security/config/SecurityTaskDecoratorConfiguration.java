package com.wsf.infrastructure.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;

@Configuration
public class SecurityTaskDecoratorConfiguration {

    @Bean
    public TaskDecorator securityContextTaskDecorator() {
        return DelegatingSecurityContextRunnable::new; // 等价于 runnable -> new
                                                       // DelegatingSecurityContextRunnable(runnable)
    }
}

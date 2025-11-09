package com.wsf.infrastructure.config;

import com.wsf.infrastructure.eventbus.EventBus;
import com.wsf.infrastructure.eventbus.EventHandlerScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;

/**
 * 事件总线配置类
 * 配置事件总线相关Bean和Spring集成
 */
@Configuration
public class EventBusConfiguration {

    /**
     * 事件总线Bean
     */
    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    /**
     * 事件处理器扫描器
     * 自动扫描和注册事件处理方法
     */
    @Bean
    public EventHandlerScanner eventHandlerScanner(EventBus eventBus, ApplicationContext applicationContext) {
        EventHandlerScanner scanner = new EventHandlerScanner(eventBus);
        
        // 设置BeanFactoryResolver以便在SpEL表达式中访问Spring Bean
        BeanFactoryResolver beanFactoryResolver = new BeanFactoryResolver(applicationContext);
        eventBus.setBeanFactoryResolver(beanFactoryResolver);
        
        return scanner;
    }
}
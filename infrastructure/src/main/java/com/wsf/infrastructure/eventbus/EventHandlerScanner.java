package com.wsf.infrastructure.eventbus;

import com.wsf.domain.events.Event;
import com.wsf.infrastructure.eventbus.annotation.EventSubscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件处理器扫描器
 * 自动扫描Spring Bean中的事件处理方法并注册到事件总线
 */
public class EventHandlerScanner implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(EventHandlerScanner.class);

    private final EventBus eventBus;
    private ApplicationContext applicationContext;
    
    // 缓存已处理的Bean和方法，避免重复注册
    private final Map<String, Boolean> processedBeans = new ConcurrentHashMap<>();

    public EventHandlerScanner(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 跳过已经处理过的Bean
        if (processedBeans.containsKey(beanName)) {
            return bean;
        }

        try {
            // 扫描Bean中的事件处理方法
            scanEventHandlers(bean, beanName);
            processedBeans.put(beanName, true);
        } catch (Exception e) {
            log.error("Failed to scan event handlers for bean: {}", beanName, e);
        }

        return bean;
    }

    /**
     * 扫描Bean中的事件处理方法
     */
    private void scanEventHandlers(Object bean, String beanName) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        
        // 使用Spring的MethodIntrospector查找所有带有@EventSubscribe注解的方法
        Map<Method, EventSubscribe> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<EventSubscribe>) method -> 
                    AnnotatedElementUtils.findMergedAnnotation(method, EventSubscribe.class));

        if (annotatedMethods.isEmpty()) {
            return;
        }

        log.debug("Found {} event handler methods in bean: {}", annotatedMethods.size(), beanName);

        for (Map.Entry<Method, EventSubscribe> entry : annotatedMethods.entrySet()) {
            Method method = entry.getKey();
            EventSubscribe annotation = entry.getValue();
            
            try {
                registerEventHandler(bean, method, annotation);
            } catch (Exception e) {
                log.error("Failed to register event handler method: {} in bean: {}", 
                         method.getName(), beanName, e);
            }
        }
    }

    /**
     * 注册事件处理方法
     */
    @SuppressWarnings("unchecked")
    private void registerEventHandler(Object bean, Method method, EventSubscribe annotation) {
        // 验证方法参数
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            log.warn("Event handler method must have exactly one parameter. Method: {}", method.getName());
            return;
        }

        // 确定事件类型
        Class<? extends Event> eventType;
        if (annotation.value() != void.class) {
            // 使用注解指定的类型
            if (!Event.class.isAssignableFrom(annotation.value())) {
                log.warn("Specified event type {} must implement Event interface. Method: {}", 
                        annotation.value().getSimpleName(), method.getName());
                return;
            }
            eventType = (Class<? extends Event>) annotation.value();
        } else {
            // 从方法参数推断类型
            Class<?> paramType = parameterTypes[0];
            if (!Event.class.isAssignableFrom(paramType)) {
                log.warn("Method parameter type {} must implement Event interface. Method: {}", 
                        paramType.getSimpleName(), method.getName());
                return;
            }
            eventType = (Class<? extends Event>) paramType;
        }

        // 创建事件处理器
        EventHandler<? extends Event> handler = event -> {
            try {
                method.setAccessible(true);
                method.invoke(bean, event);
            } catch (Exception e) {
                log.error("Error invoking event handler method: {} for event: {}", 
                         method.getName(), event.getClass().getSimpleName(), e);
                throw new RuntimeException("Event handler invocation failed", e);
            }
        };

        // 注册到事件总线
        eventBus.register(eventType, (EventHandler<Event>) handler, annotation);
        
        log.debug("Registered event handler: {}.{} for event type: {}", 
                 bean.getClass().getSimpleName(), method.getName(), eventType.getSimpleName());
    }

    /**
     * 手动扫描并注册所有事件处理器
     * 可用于在应用启动完成后重新扫描
     */
    public void scanAndRegisterAllHandlers() {
        processedBeans.clear();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                scanEventHandlers(bean, beanName);
            } catch (Exception e) {
                log.error("Failed to scan event handlers for bean: {}", beanName, e);
            }
        }
        
        log.info("Completed scanning event handlers, found {} beans with event handlers", 
                processedBeans.size());
    }
}
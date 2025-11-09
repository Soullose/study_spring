package com.wsf.infrastructure.eventbus;

import com.wsf.domain.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.BeanFactoryResolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件总线核心实现
 * 支持同步事件分发和基于注解的过滤机制
 */
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    /**
     * 事件处理器注册表
     * key: 事件类型
     * value: 该事件类型对应的处理器包装器列表
     */
    private final ConcurrentMap<Class<? extends Event>, List<EventHandlerWrapper<?>>> handlers;

    /**
     * Spring Bean 解析器，用于 SpEL 表达式中的 Bean 访问
     */
    private BeanFactoryResolver beanFactoryResolver;

    public EventBus() {
        this.handlers = new ConcurrentHashMap<>();
    }

    /**
     * 发布事件
     * @param event 要发布的事件
     */
    public void publish(Event event) {
        if (event == null) {
            log.warn("Attempted to publish null event");
            return;
        }

        Class<? extends Event> eventType = event.getClass();
        List<EventHandlerWrapper<?>> eventHandlers = handlers.get(eventType);

        if (eventHandlers == null || eventHandlers.isEmpty()) {
            log.debug("No handlers found for event type: {}", eventType.getSimpleName());
            return;
        }

        log.debug("Publishing event: {} to {} handlers", eventType.getSimpleName(), eventHandlers.size());

        // 同步执行所有处理器
        for (EventHandlerWrapper<?> wrapper : eventHandlers) {
            try {
                handleEvent(wrapper, event);
            } catch (Exception e) {
                log.error("Error handling event {} with handler {}", eventType.getSimpleName(), 
                         wrapper.getHandler().getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 注册事件处理器
     * @param eventType 事件类型
     * @param handler 事件处理器
     * @param annotation 事件订阅注解
     */
    public <T extends Event> void register(Class<T> eventType, EventHandler<T> handler, 
                                          com.wsf.infrastructure.eventbus.annotation.EventSubscribe annotation) {
        if (eventType == null || handler == null) {
            throw new IllegalArgumentException("Event type and handler cannot be null");
        }

        EventHandlerWrapper<T> wrapper = new EventHandlerWrapper<>(handler, annotation);
        
        // 设置 BeanFactoryResolver 以便在 SpEL 表达式中访问 Spring Bean
        if (beanFactoryResolver != null) {
            wrapper.setBeanFactoryResolver(beanFactoryResolver);
        }

        handlers.compute(eventType, (key, existingHandlers) -> {
            List<EventHandlerWrapper<?>> handlerList = existingHandlers != null ? 
                new CopyOnWriteArrayList<>(existingHandlers) : new CopyOnWriteArrayList<>();
            
            handlerList.add(wrapper);
            
            // 按优先级排序
            handlerList.sort(Comparator.comparingInt(EventHandlerWrapper::getOrder));
            
            return handlerList;
        });

        log.debug("Registered handler for event type: {}", eventType.getSimpleName());
    }

    /**
     * 取消注册事件处理器
     * @param eventType 事件类型
     * @param handler 要移除的事件处理器
     */
    public <T extends Event> void unregister(Class<T> eventType, EventHandler<T> handler) {
        if (eventType == null || handler == null) {
            return;
        }

        handlers.computeIfPresent(eventType, (key, existingHandlers) -> {
            List<EventHandlerWrapper<?>> newHandlers = new CopyOnWriteArrayList<>(existingHandlers);
            newHandlers.removeIf(wrapper -> wrapper.getHandler().equals(handler));
            
            return newHandlers.isEmpty() ? null : newHandlers;
        });

        log.debug("Unregistered handler for event type: {}", eventType.getSimpleName());
    }

    /**
     * 设置 BeanFactoryResolver
     */
    public void setBeanFactoryResolver(BeanFactoryResolver beanFactoryResolver) {
        this.beanFactoryResolver = beanFactoryResolver;
    }

    /**
     * 获取指定事件类型的处理器数量
     */
    public int getHandlerCount(Class<? extends Event> eventType) {
        List<EventHandlerWrapper<?>> eventHandlers = handlers.get(eventType);
        return eventHandlers != null ? eventHandlers.size() : 0;
    }

    /**
     * 获取所有已注册的事件类型
     */
    public List<Class<? extends Event>> getRegisteredEventTypes() {
        return new ArrayList<>(handlers.keySet());
    }

    /**
     * 处理单个事件（类型安全的处理）
     */
    @SuppressWarnings("unchecked")
    private <T extends Event> void handleEvent(EventHandlerWrapper<?> wrapper, T event) {
        EventHandlerWrapper<T> typedWrapper = (EventHandlerWrapper<T>) wrapper;
        typedWrapper.handle(event);
    }
}
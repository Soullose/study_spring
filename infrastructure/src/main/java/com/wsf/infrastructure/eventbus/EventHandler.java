package com.wsf.infrastructure.eventbus;

import com.wsf.domain.events.Event;

/**
 * 事件处理器接口
 * 泛型 T 表示处理的事件类型
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {

    /**
     * 处理事件
     * @param event 要处理的事件
     */
    void handle(T event);
}
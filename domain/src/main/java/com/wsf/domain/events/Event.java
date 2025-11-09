package com.wsf.domain.events;

/**
 * 事件接口
 * 所有领域事件都应实现此接口
 */
public interface Event {

    /**
     * 获取事件源对象
     */
    Object getSource();

    /**
     * 获取事件发生时间戳
     */
    long getTimestamp();
}
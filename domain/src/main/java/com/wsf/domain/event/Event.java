package com.wsf.domain.event;

/**
 * 领域事件接口
 */
public interface Event {
    
    /**
     * 获取事件源
     */
    Object getSource();
    
    /**
     * 获取事件时间戳
     */
    long getTimestamp();
    
    /**
     * 获取事件类型
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}

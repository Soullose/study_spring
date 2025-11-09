package com.wsf.domain.events;

import java.time.Instant;

/**
 * 基础事件抽象类
 * 提供事件源和时间戳的默认实现
 */
public abstract class BaseEvent implements Event {

    private final Object source;
    private final long timestamp;

    protected BaseEvent(Object source) {
        this.source = source;
        this.timestamp = Instant.now().toEpochMilli();
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "source=" + source +
                ", timestamp=" + timestamp +
                '}';
    }
}
package com.wsf.domain.event;

import java.time.Instant;

/**
 * 统一领域事件基类（继承 Spring ApplicationEvent）。
 * <p>
 * 所有领域事件必须继承此类，以获得 Spring Event 生态集成能力：
 * <ul>
 *   <li>通过 {@code ApplicationEventPublisher} 发布事件</li>
 *   <li>通过 {@code @EventListener} 或 {@code @TransactionalEventListener} 订阅事件</li>
 *   <li>支持 {@code @Async} 异步执行</li>
 *   <li>支持 {@code @Order} 控制执行顺序</li>
 *   <li>支持 SpEL {@code condition} 条件过滤</li>
 * </ul>
 *
 * @author wsf
 * @since 1.0
 */
public abstract class BaseDomainEvent extends ApplicationEvent {

    /** 事件唯一标识（用于追踪和去重） */
    private final String eventId;

    /** 事件发生时间戳（epoch millis） */
    private final long timestamp;

    /**
     * 构造领域事件。
     *
     * @param source 事件源对象（发布事件的 Bean 或聚合根）
     */
    protected BaseDomainEvent(Object source) {
        super(source);
        this.eventId = java.util.UUID.randomUUID().toString();
        this.timestamp = Instant.now().toEpochMilli();
    }

    /**
     * 获取事件唯一标识。
     *
     * @return 事件ID（UUID v4）
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 获取事件发生时间戳。
     *
     * @return epoch 毫秒时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 获取事件类型名称（默认为类名）。
     *
     * @return 事件类型字符串
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", source=" + getSource() +
                '}';
    }
}

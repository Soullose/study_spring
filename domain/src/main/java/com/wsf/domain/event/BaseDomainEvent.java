package com.wsf.domain.event;

import java.time.Instant;

import org.springframework.context.ApplicationEvent;

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
 * <p>
 * 注意：Spring 6.x 中 {@code ApplicationEvent.getTimestamp()} 是 final 方法，
 * 本类使用 {@code eventTimestamp} 字段存储自定义时间戳，通过 {@link #getEventTimestamp()} 访问。
 * </p>
 *
 * @author wsf
 * @since 1.0
 */
public abstract class BaseDomainEvent extends ApplicationEvent {

    /** 事件唯一标识（用于追踪和去重） */
    private final String eventId;

    /** 事件发生时间戳（epoch millis），因 ApplicationEvent.getTimestamp() 为 final，使用独立字段 */
    private final long eventTimestamp;

    /**
     * 构造领域事件。
     *
     * @param source 事件源对象（发布事件的 Bean 或聚合根）
     */
    protected BaseDomainEvent(Object source) {
        super(source);
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventTimestamp = Instant.now().toEpochMilli();
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
     * 获取自定义事件时间戳（epoch 毫秒）。
     * <p>
     * 与父类 {@code ApplicationEvent.getTimestamp()} 不同，此方法返回的是构造时记录的瞬时时间。
     * </p>
     *
     * @return epoch 毫秒时间戳
     */
    public long getEventTimestamp() {
        return eventTimestamp;
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
                ", eventTimestamp=" + eventTimestamp +
                ", timestamp=" + super.getTimestamp() +
                ", source=" + getSource() +
                '}';
    }
}

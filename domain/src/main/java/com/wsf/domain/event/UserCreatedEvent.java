package com.wsf.domain.event;

import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.UserName;

/**
 * 用户创建事件（用户域）。
 * <p>
 * 当新用户被创建时发布此事件。实现了 {@link UserDomainEventType} 以便按用户域统一监听。
 * </p>
 *
 * @author wsf
 */
public class UserCreatedEvent extends BaseDomainEvent implements UserDomainEventType {

    private final String userId;
    private final UserName name;
    private final Email email;

    /**
     * @param source 事件源（发布事件的 Bean 或聚合根）
     * @param userId 用户ID
     * @param name   用户名
     * @param email  邮箱
     */
    public UserCreatedEvent(Object source, String userId, UserName name, Email email) {
        super(source);
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public UserName getName() { return name; }
    public Email getEmail() { return email; }

    @Override
    public String toString() {
        return "UserCreatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", userId='" + userId + '\'' +
                ", name=" + name +
                ", email=" + email +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

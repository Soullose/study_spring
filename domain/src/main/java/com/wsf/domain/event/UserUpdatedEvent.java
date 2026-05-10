package com.wsf.domain.event;

import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.UserName;

/**
 * 用户更新事件（用户域）。
 * <p>
 * 当用户信息发生变更时发布此事件。
 * </p>
 *
 * @author wsf
 */
public class UserUpdatedEvent extends BaseDomainEvent implements UserDomainEventType {

    private final String userId;
    private final UserName name;
    private final Email email;

    /**
     * @param source 事件源
     * @param userId 用户ID
     * @param name   更新后的用户名
     * @param email  更新后的邮箱
     */
    public UserUpdatedEvent(Object source, String userId, UserName name, Email email) {
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
        return "UserUpdatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", userId='" + userId + '\'' +
                ", name=" + name +
                ", email=" + email +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

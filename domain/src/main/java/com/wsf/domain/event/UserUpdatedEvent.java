package com.wsf.domain.event;

import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.UserName;

/**
 * 用户更新事件（用户域）。
 *
 * @author wsf
 */
public class UserUpdatedEvent extends BaseDomainEvent implements UserDomainEventType {

    private final String userId;
    private final UserName name;
    private final Email email;

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
                ", eventTimestamp=" + getEventTimestamp() +
                '}';
    }
}

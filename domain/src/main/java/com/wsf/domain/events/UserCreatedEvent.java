package com.wsf.domain.events;

/**
 * 用户创建事件示例
 */
public class UserCreatedEvent extends BaseEvent {

    private final Long userId;
    private final String username;
    private final String email;

    public UserCreatedEvent(Object source, Long userId, String username, String email) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "UserCreatedEvent{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
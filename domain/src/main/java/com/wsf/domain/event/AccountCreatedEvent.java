package com.wsf.domain.event;

/**
 * 账户创建事件
 */
public class AccountCreatedEvent extends BaseEvent {

    private final String accountId;
    private final String username;
    private final String userId;

    public AccountCreatedEvent(Object source, String accountId, String username, String userId) {
        super(source);
        this.accountId = accountId;
        this.username = username;
        this.userId = userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "AccountCreatedEvent{" +
                "accountId='" + accountId + '\'' +
                ", username='" + username + '\'' +
                ", userId='" + userId + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

package com.wsf.domain.event;

/**
 * 账户锁定事件
 */
public class AccountLockedEvent extends BaseEvent {

    private final String accountId;
    private final String username;
    private final String reason;

    public AccountLockedEvent(Object source, String accountId, String username, String reason) {
        super(source);
        this.accountId = accountId;
        this.username = username;
        this.reason = reason;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "AccountLockedEvent{" +
                "accountId='" + accountId + '\'' +
                ", username='" + username + '\'' +
                ", reason='" + reason + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

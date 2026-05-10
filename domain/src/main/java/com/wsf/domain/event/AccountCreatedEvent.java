package com.wsf.domain.event;

/**
 * 账户创建事件（账户域）。
 * <p>
 * 当用户账户被创建时发布此事件。
 * </p>
 *
 * @author wsf
 */
public class AccountCreatedEvent extends BaseDomainEvent implements AccountDomainEventType {

    private final String accountId;
    private final String username;
    private final String userId;

    /**
     * @param source    事件源
     * @param accountId 账户ID
     * @param username  用户名
     * @param userId    关联的用户ID
     */
    public AccountCreatedEvent(Object source, String accountId, String username, String userId) {
        super(source);
        this.accountId = accountId;
        this.username = username;
        this.userId = userId;
    }

    public String getAccountId() { return accountId; }
    public String getUsername() { return username; }
    public String getUserId() { return userId; }

    @Override
    public String toString() {
        return "AccountCreatedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", accountId='" + accountId + '\'' +
                ", username='" + username + '\'' +
                ", userId='" + userId + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

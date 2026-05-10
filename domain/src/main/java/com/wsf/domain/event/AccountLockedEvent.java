package com.wsf.domain.event;

/**
 * 账户锁定事件（账户域）。
 * <p>
 * 当用户账户被锁定时发布此事件（如登录失败次数过多）。
 * </p>
 *
 * @author wsf
 */
public class AccountLockedEvent extends BaseDomainEvent implements AccountDomainEventType {

    private final String accountId;
    private final String username;
    private final String reason;

    /**
     * @param source    事件源
     * @param accountId 账户ID
     * @param username  用户名
     * @param reason    锁定原因
     */
    public AccountLockedEvent(Object source, String accountId, String username, String reason) {
        super(source);
        this.accountId = accountId;
        this.username = username;
        this.reason = reason;
    }

    public String getAccountId() { return accountId; }
    public String getUsername() { return username; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return "AccountLockedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", accountId='" + accountId + '\'' +
                ", username='" + username + '\'' +
                ", reason='" + reason + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

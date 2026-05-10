package com.wsf.domain.event;

import java.util.Set;

/**
 * 角色分配事件（系统域——不实现特定领域标记接口，属于跨领域系统事件）。
 *
 * @author wsf
 */
public class RoleAssignedEvent extends BaseDomainEvent {

    private final String accountId;
    private final Set<String> roleIds;
    private final Set<String> roleCodes;

    public RoleAssignedEvent(Object source, String accountId, Set<String> roleIds, Set<String> roleCodes) {
        super(source);
        this.accountId = accountId;
        this.roleIds = roleIds;
        this.roleCodes = roleCodes;
    }

    public String getAccountId() { return accountId; }
    public Set<String> getRoleIds() { return roleIds; }
    public Set<String> getRoleCodes() { return roleCodes; }

    @Override
    public String toString() {
        return "RoleAssignedEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", accountId='" + accountId + '\'' +
                ", roleIds=" + roleIds +
                ", roleCodes=" + roleCodes +
                ", eventTimestamp=" + getEventTimestamp() +
                '}';
    }
}

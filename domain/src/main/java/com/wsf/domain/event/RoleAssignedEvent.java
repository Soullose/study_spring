package com.wsf.domain.event;

import java.util.Set;

/**
 * 角色分配事件（系统域——不实现特定领域标记接口，属于跨领域系统事件）。
 * <p>
 * 当用户被分配或变更角色时发布此事件。
 * </p>
 *
 * @author wsf
 */
public class RoleAssignedEvent extends BaseDomainEvent {

    private final String accountId;
    private final Set<String> roleIds;
    private final Set<String> roleCodes;

    /**
     * @param source    事件源
     * @param accountId 账户ID
     * @param roleIds   分配的角色ID集合
     * @param roleCodes 分配的角色编码集合
     */
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
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

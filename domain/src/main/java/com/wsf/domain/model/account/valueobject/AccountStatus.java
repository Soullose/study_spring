package com.wsf.domain.model.account.valueobject;

/**
 * 账户状态值对象
 * 封装账户的各种状态
 */
public record AccountStatus(
    boolean enabled,
    boolean accountNonExpired,
    boolean accountNonLocked,
    boolean credentialsNonExpired
) {
    
    /**
     * 正常状态
     */
    public static AccountStatus normal() {
        return new AccountStatus(true, true, true, true);
    }
    
    /**
     * 禁用状态
     */
    public static AccountStatus disabled() {
        return new AccountStatus(false, true, true, true);
    }
    
    /**
     * 锁定状态
     */
    public static AccountStatus locked() {
        return new AccountStatus(true, true, false, true);
    }
    
    /**
     * 过期状态
     */
    public static AccountStatus expired() {
        return new AccountStatus(true, false, true, true);
    }
    
    /**
     * 凭证过期状态
     */
    public static AccountStatus credentialsExpired() {
        return new AccountStatus(true, true, true, false);
    }
    
    /**
     * 账户是否可用
     */
    public boolean isAvailable() {
        return enabled && accountNonExpired && accountNonLocked && credentialsNonExpired;
    }
    
    /**
     * 启用账户
     */
    public AccountStatus enable() {
        return new AccountStatus(true, accountNonExpired, accountNonLocked, credentialsNonExpired);
    }
    
    /**
     * 禁用账户
     */
    public AccountStatus disable() {
        return new AccountStatus(false, accountNonExpired, accountNonLocked, credentialsNonExpired);
    }
    
    /**
     * 锁定账户
     */
    public AccountStatus lock() {
        return new AccountStatus(enabled, accountNonExpired, false, credentialsNonExpired);
    }
    
    /**
     * 解锁账户
     */
    public AccountStatus unlock() {
        return new AccountStatus(enabled, accountNonExpired, true, credentialsNonExpired);
    }
}

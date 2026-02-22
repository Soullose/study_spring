package com.wsf.domain.model.account.aggregate;

import com.wsf.domain.model.account.valueobject.*;
import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.role.aggregate.Role;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * UserAccount聚合根
 * 表示账户信息，用于登录系统，可选关联User
 */
@Getter
public class UserAccount {
    
    /**
     * 账户ID
     */
    private final String id;
    
    /**
     * 用户名
     */
    private final String username;
    
    /**
     * 密码
     */
    private Password password;
    
    /**
     * 账户状态
     */
    private AccountStatus status;
    
    /**
     * 关联的用户ID（可选）
     */
    private String userId;
    
    /**
     * 关联的角色列表
     */
    private final Set<Role> roles = new HashSet<>();
    
    /**
     * 用户补充菜单权限
     */
    private final Set<Menu> supplementaryMenus = new HashSet<>();
    
    /**
     * 用户补充数据权限
     */
    private final Set<DataPermission> supplementaryDataPermissions = new HashSet<>();
    
    /**
     * 创建时间
     */
    private final LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 私有构造函数
     */
    private UserAccount(String id, String username, Password password, 
                        AccountStatus status, String userId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.userId = userId;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }
    
    /**
     * 创建账户（工厂方法）
     */
    public static UserAccount create(String id, String username, Password password, String userId) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        return new UserAccount(id, username, password, AccountStatus.normal(), userId);
    }
    
    /**
     * 重建账户（从持久化层恢复）
     */
    public static UserAccount rebuild(String id, String username, Password password,
                                      AccountStatus status, String userId,
                                      LocalDateTime createTime, LocalDateTime updateTime) {
        UserAccount account = new UserAccount(id, username, password, status, userId);
        try {
            var field = UserAccount.class.getDeclaredField("createTime");
            field.setAccessible(true);
            field.set(account, createTime);
        } catch (Exception ignored) {
        }
        account.updateTime = updateTime;
        return account;
    }
    
    /**
     * 更新密码
     */
    public void updatePassword(Password newPassword) {
        this.password = newPassword;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 关联用户
     */
    public void linkUser(String userId) {
        this.userId = userId;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 解除用户关联
     */
    public void unlinkUser() {
        this.userId = null;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 启用账户
     */
    public void enable() {
        this.status = status.enable();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 禁用账户
     */
    public void disable() {
        this.status = status.disable();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 锁定账户
     */
    public void lock() {
        this.status = status.lock();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 解锁账户
     */
    public void unlock() {
        this.status = status.unlock();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 分配角色
     */
    public void assignRole(Role role) {
        this.roles.add(role);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 移除角色
     */
    public void removeRole(String roleId) {
        this.roles.removeIf(role -> role.getId().equals(roleId));
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 批量分配角色
     */
    public void assignRoles(Set<Role> roles) {
        this.roles.addAll(roles);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 清空角色
     */
    public void clearRoles() {
        this.roles.clear();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 添加补充菜单权限
     */
    public void addSupplementaryMenu(Menu menu) {
        this.supplementaryMenus.add(menu);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 移除补充菜单权限
     */
    public void removeSupplementaryMenu(String menuId) {
        this.supplementaryMenus.removeIf(menu -> menu.getId().equals(menuId));
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 添加补充数据权限
     */
    public void addSupplementaryDataPermission(DataPermission permission) {
        this.supplementaryDataPermissions.add(permission);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 移除补充数据权限
     */
    public void removeSupplementaryDataPermission(String permissionId) {
        this.supplementaryDataPermissions.removeIf(p -> p.getId().equals(permissionId));
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 账户是否可用
     */
    public boolean isAvailable() {
        return status.isAvailable();
    }
    
    /**
     * 获取角色ID列表
     */
    public Set<String> getRoleIds() {
        return roles.stream().map(Role::getId).collect(java.util.stream.Collectors.toSet());
    }
}

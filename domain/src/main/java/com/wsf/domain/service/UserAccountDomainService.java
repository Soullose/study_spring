package com.wsf.domain.service;

import com.wsf.domain.model.account.aggregate.UserAccount;
import com.wsf.domain.model.account.valueobject.Password;
import com.wsf.domain.model.role.aggregate.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 账户领域服务
 * 处理账户相关的领域逻辑
 */
public interface UserAccountDomainService {
    
    /**
     * 创建账户
     */
    UserAccount createAccount(String username, Password password, String userId);
    
    /**
     * 更新密码
     */
    UserAccount updatePassword(String accountId, Password newPassword);
    
    /**
     * 关联用户
     */
    UserAccount linkUser(String accountId, String userId);
    
    /**
     * 解除用户关联
     */
    UserAccount unlinkUser(String accountId);
    
    /**
     * 启用账户
     */
    UserAccount enableAccount(String accountId);
    
    /**
     * 禁用账户
     */
    UserAccount disableAccount(String accountId);
    
    /**
     * 锁定账户
     */
    UserAccount lockAccount(String accountId);
    
    /**
     * 解锁账户
     */
    UserAccount unlockAccount(String accountId);
    
    /**
     * 分配角色
     */
    UserAccount assignRole(String accountId, Role role);
    
    /**
     * 批量分配角色
     */
    UserAccount assignRoles(String accountId, Set<Role> roles);
    
    /**
     * 移除角色
     */
    UserAccount removeRole(String accountId, String roleId);
    
    /**
     * 清空角色
     */
    UserAccount clearRoles(String accountId);
    
    /**
     * 根据ID查找账户
     */
    Optional<UserAccount> findById(String accountId);
    
    /**
     * 根据用户名查找账户
     */
    Optional<UserAccount> findByUsername(String username);
    
    /**
     * 根据用户ID查找账户
     */
    Optional<UserAccount> findByUserId(String userId);
    
    /**
     * 查找所有账户
     */
    List<UserAccount> findAll();
    
    /**
     * 根据角色ID查找账户
     */
    List<UserAccount> findByRoleId(String roleId);
    
    /**
     * 删除账户
     */
    void deleteAccount(String accountId);
    
    /**
     * 检查用户名是否存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查用户是否已关联账户
     */
    boolean isUserLinkedToAccount(String userId);
    
    /**
     * 验证账户密码
     */
    boolean validatePassword(String accountId, Password password);
}

package com.wsf.domain.repository;

import com.wsf.domain.model.account.aggregate.UserAccount;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 账户仓储接口
 */
public interface UserAccountRepository {
    
    /**
     * 保存账户
     */
    UserAccount save(UserAccount account);
    
    /**
     * 批量保存账户
     */
    List<UserAccount> saveAll(List<UserAccount> accounts);
    
    /**
     * 根据ID查找账户
     */
    Optional<UserAccount> findById(String id);
    
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
     * 根据角色ID查找账户列表
     */
    List<UserAccount> findByRoleId(String roleId);
    
    /**
     * 根据ID列表查找账户
     */
    List<UserAccount> findByIds(List<String> ids);
    
    /**
     * 删除账户
     */
    void deleteById(String id);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查用户是否已关联账户
     */
    boolean existsByUserId(String userId);
    
    /**
     * 查找账户及其关联的角色
     */
    Optional<UserAccount> findByIdWithRoles(String id);
    
    /**
     * 查找账户及其关联的补充菜单
     */
    Optional<UserAccount> findByIdWithSupplementaryMenus(String id);
    
    /**
     * 查找账户及其关联的补充数据权限
     */
    Optional<UserAccount> findByIdWithSupplementaryDataPermissions(String id);
    
    /**
     * 查找账户的完整信息（包含所有关联）
     */
    Optional<UserAccount> findByIdWithAllRelations(String id);
}

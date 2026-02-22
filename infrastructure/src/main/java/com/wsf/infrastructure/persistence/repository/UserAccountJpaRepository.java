package com.wsf.infrastructure.persistence.repository;

import com.wsf.infrastructure.persistence.entity.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户账户JPA仓储
 */
@Repository
public interface UserAccountJpaRepository 
        extends JpaRepository<UserAccount, String>, 
                JpaSpecificationExecutor<UserAccount>,
                QuerydslPredicateExecutor<UserAccount> {
    
    /**
     * 根据用户名查找账户
     */
    Optional<UserAccount> findByUsername(String username);
    
    /**
     * 根据用户名查找账户（带角色信息）
     */
    @Query("SELECT ua FROM UserAccount ua LEFT JOIN FETCH ua.roles WHERE ua.username = :username")
    Optional<UserAccount> findByUsernameWithRoles(String username);
    
    /**
     * 根据用户ID查找账户
     */
    @Query("SELECT ua FROM UserAccount ua WHERE ua.user.id = :userId")
    Optional<UserAccount> findByUserId(String userId);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 查找所有启用的账户
     */
    List<UserAccount> findByEnabledTrue();
    
    /**
     * 查找所有未锁定的账户
     */
    List<UserAccount> findByAccountNonLockedTrue();
    
    /**
     * 根据角色ID查找账户列表
     */
    @Query("SELECT ua FROM UserAccount ua JOIN ua.roles r WHERE r.id = :roleId")
    List<UserAccount> findByRoleId(String roleId);
    
    /**
     * 查找拥有指定角色编码的账户
     */
    @Query("SELECT ua FROM UserAccount ua JOIN ua.roles r WHERE r.code = :roleCode")
    List<UserAccount> findByRoleCode(String roleCode);
}

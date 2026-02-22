package com.wsf.infrastructure.persistence.repository;

import com.wsf.infrastructure.persistence.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色JPA仓储
 */
@Repository
public interface RoleJpaRepository 
        extends JpaRepository<Role, String>, 
                JpaSpecificationExecutor<Role>,
                QuerydslPredicateExecutor<Role> {
    
    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByCode(String code);
    
    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByName(String name);
    
    /**
     * 检查角色编码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据账户ID查找角色列表
     */
    @Query("SELECT r FROM Role r JOIN r.userAccounts ua WHERE ua.id = :userAccountId")
    Set<Role> findByUserAccountId(String userAccountId);
    
    /**
     * 根据多个角色ID查找角色列表
     */
    Set<Role> findByIdIn(Set<String> ids);
    
    /**
     * 查找所有角色（按名称排序）
     */
    List<Role> findAllByOrderByNameAsc();
}

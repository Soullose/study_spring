package com.wsf.infrastructure.persistence.repository;

import com.wsf.infrastructure.persistence.entity.permission.PermissionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限JPA仓储
 */
@Repository
public interface PermissionJpaRepository 
        extends JpaRepository<PermissionPO, String>, 
                JpaSpecificationExecutor<PermissionPO>,
                QuerydslPredicateExecutor<PermissionPO> {
    
    /**
     * 根据权限编码查找权限
     */
    Optional<PermissionPO> findByPermissionCode(String permissionCode);
    
    /**
     * 根据菜单ID查找权限列表
     */
    List<PermissionPO> findByMenuId(String menuId);
    
    /**
     * 根据资源标识查找权限列表
     */
    List<PermissionPO> findByResource(String resource);
    
    /**
     * 查找启用的权限列表
     */
    List<PermissionPO> findByEnabledTrue();
    
    /**
     * 检查权限编码是否存在
     */
    boolean existsByPermissionCode(String permissionCode);
}

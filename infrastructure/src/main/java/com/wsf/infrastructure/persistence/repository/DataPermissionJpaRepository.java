package com.wsf.infrastructure.persistence.repository;

import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import com.wsf.infrastructure.persistence.entity.datapermission.DataPermissionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据权限JPA仓储
 */
@Repository
public interface DataPermissionJpaRepository 
        extends JpaRepository<DataPermissionPO, String>, 
                JpaSpecificationExecutor<DataPermissionPO>,
                QuerydslPredicateExecutor<DataPermissionPO> {
    
    /**
     * 根据数据范围查找数据权限列表
     */
    List<DataPermissionPO> findByDataScope(DataScope dataScope);
    
    /**
     * 根据资源类型查找数据权限列表
     */
    List<DataPermissionPO> findByResourceType(ResourceType resourceType);
    
    /**
     * 查找启用的数据权限列表
     */
    List<DataPermissionPO> findByEnabledTrue();
    
    /**
     * 检查权限名称是否存在
     */
    boolean existsByPermissionName(String permissionName);
}

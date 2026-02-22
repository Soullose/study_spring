package com.wsf.domain.service;

import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 数据权限领域服务
 * 处理数据权限相关的领域逻辑
 */
public interface DataPermissionDomainService {
    
    /**
     * 创建数据权限
     */
    DataPermission createPermission(String name, ResourceType resourceType,
                                    DataScope dataScope, String description);
    
    /**
     * 创建自定义数据权限
     */
    DataPermission createCustomPermission(String name, ResourceType resourceType,
                                          Set<String> resourceIds, String description);
    
    /**
     * 更新数据权限
     */
    DataPermission updatePermission(String permissionId, String name,
                                    DataScope dataScope, String description);
    
    /**
     * 更新自定义资源ID列表
     */
    DataPermission updateResourceIds(String permissionId, Set<String> resourceIds);
    
    /**
     * 启用数据权限
     */
    DataPermission enablePermission(String permissionId);
    
    /**
     * 禁用数据权限
     */
    DataPermission disablePermission(String permissionId);
    
    /**
     * 根据ID查找数据权限
     */
    Optional<DataPermission> findById(String permissionId);
    
    /**
     * 查找所有数据权限
     */
    List<DataPermission> findAll();
    
    /**
     * 查找启用的数据权限
     */
    List<DataPermission> findAllEnabled();
    
    /**
     * 根据数据范围查找数据权限
     */
    List<DataPermission> findByDataScope(DataScope dataScope);
    
    /**
     * 根据资源类型查找数据权限
     */
    List<DataPermission> findByResourceType(ResourceType resourceType);
    
    /**
     * 根据ID集合查找数据权限
     */
    Set<DataPermission> findByIds(Set<String> ids);
    
    /**
     * 删除数据权限
     */
    void deletePermission(String permissionId);
    
    /**
     * 检查权限名称是否存在
     */
    boolean isNameExists(String name);
}

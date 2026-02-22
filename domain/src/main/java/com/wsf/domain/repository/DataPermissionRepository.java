package com.wsf.domain.repository;

import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 数据权限仓储接口
 */
public interface DataPermissionRepository {
    
    /**
     * 保存数据权限
     */
    DataPermission save(DataPermission permission);
    
    /**
     * 批量保存数据权限
     */
    List<DataPermission> saveAll(List<DataPermission> permissions);
    
    /**
     * 根据ID查找数据权限
     */
    Optional<DataPermission> findById(String id);
    
    /**
     * 查找所有数据权限
     */
    List<DataPermission> findAll();
    
    /**
     * 根据ID列表查找数据权限
     */
    List<DataPermission> findByIds(List<String> ids);
    
    /**
     * 根据ID集合查找数据权限
     */
    Set<DataPermission> findByIds(Set<String> ids);
    
    /**
     * 根据数据范围查找数据权限
     */
    List<DataPermission> findByDataScope(DataScope dataScope);
    
    /**
     * 根据资源类型查找数据权限
     */
    List<DataPermission> findByResourceType(ResourceType resourceType);
    
    /**
     * 查找启用的数据权限
     */
    List<DataPermission> findAllEnabled();
    
    /**
     * 删除数据权限
     */
    void deleteById(String id);
    
    /**
     * 检查权限名称是否存在
     */
    boolean existsByName(String name);
}

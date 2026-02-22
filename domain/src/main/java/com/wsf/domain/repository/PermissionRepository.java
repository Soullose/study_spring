package com.wsf.domain.repository;

import com.wsf.domain.model.permission.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限仓储接口
 */
public interface PermissionRepository {
    
    /**
     * 保存权限
     */
    Permission save(Permission permission);
    
    /**
     * 批量保存权限
     */
    List<Permission> saveAll(List<Permission> permissions);
    
    /**
     * 根据ID查找权限
     */
    Optional<Permission> findById(String id);
    
    /**
     * 根据权限编码查找权限
     */
    Optional<Permission> findByCode(String code);
    
    /**
     * 查找所有权限
     */
    List<Permission> findAll();
    
    /**
     * 根据ID列表查找权限
     */
    List<Permission> findByIds(List<String> ids);
    
    /**
     * 根据ID集合查找权限
     */
    Set<Permission> findByIds(Set<String> ids);
    
    /**
     * 根据菜单ID查找权限
     */
    List<Permission> findByMenuId(String menuId);
    
    /**
     * 根据资源标识查找权限
     */
    List<Permission> findByResource(String resource);
    
    /**
     * 查找启用的权限
     */
    List<Permission> findAllEnabled();
    
    /**
     * 删除权限
     */
    void deleteById(String id);
    
    /**
     * 检查权限编码是否存在
     */
    boolean existsByCode(String code);
}

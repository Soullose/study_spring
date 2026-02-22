package com.wsf.domain.service;

import com.wsf.domain.model.permission.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限领域服务
 * 处理权限相关的领域逻辑
 */
public interface PermissionDomainService {
    
    /**
     * 创建权限
     */
    Permission createPermission(String code, String name, String resource, 
                                String action, String menuId, String description);
    
    /**
     * 更新权限
     */
    Permission updatePermission(String permissionId, String name, String resource,
                                String action, String description);
    
    /**
     * 关联菜单
     */
    Permission linkMenu(String permissionId, String menuId);
    
    /**
     * 启用权限
     */
    Permission enablePermission(String permissionId);
    
    /**
     * 禁用权限
     */
    Permission disablePermission(String permissionId);
    
    /**
     * 根据ID查找权限
     */
    Optional<Permission> findById(String permissionId);
    
    /**
     * 根据权限编码查找权限
     */
    Optional<Permission> findByCode(String code);
    
    /**
     * 查找所有权限
     */
    List<Permission> findAll();
    
    /**
     * 查找启用的权限
     */
    List<Permission> findAllEnabled();
    
    /**
     * 根据菜单ID查找权限
     */
    List<Permission> findByMenuId(String menuId);
    
    /**
     * 根据资源标识查找权限
     */
    List<Permission> findByResource(String resource);
    
    /**
     * 根据ID集合查找权限
     */
    Set<Permission> findByIds(Set<String> ids);
    
    /**
     * 删除权限
     */
    void deletePermission(String permissionId);
    
    /**
     * 检查权限编码是否存在
     */
    boolean isCodeExists(String code);
    
    /**
     * 检查权限编码是否已被其他权限使用
     */
    boolean isCodeUsedByOtherPermission(String permissionId, String code);
}

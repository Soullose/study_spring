package com.wsf.domain.service;

import com.wsf.domain.model.role.aggregate.Role;
import com.wsf.domain.model.role.valueobject.RoleCode;
import com.wsf.domain.model.role.valueobject.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色领域服务
 * 处理角色相关的领域逻辑
 */
public interface RoleDomainService {
    
    /**
     * 创建角色
     */
    Role createRole(RoleCode code, RoleName name, String description);
    
    /**
     * 更新角色
     */
    Role updateRole(String roleId, RoleName name, String description);
    
    /**
     * 启用角色
     */
    Role enableRole(String roleId);
    
    /**
     * 禁用角色
     */
    Role disableRole(String roleId);
    
    /**
     * 分配菜单给角色
     */
    Role assignMenu(String roleId, String menuId);
    
    /**
     * 批量分配菜单给角色
     */
    Role assignMenus(String roleId, Set<String> menuIds);
    
    /**
     * 移除角色的菜单
     */
    Role removeMenu(String roleId, String menuId);
    
    /**
     * 清空角色的菜单
     */
    Role clearMenus(String roleId);
    
    /**
     * 分配数据权限给角色
     */
    Role assignDataPermission(String roleId, String permissionId);
    
    /**
     * 批量分配数据权限给角色
     */
    Role assignDataPermissions(String roleId, Set<String> permissionIds);
    
    /**
     * 移除角色的数据权限
     */
    Role removeDataPermission(String roleId, String permissionId);
    
    /**
     * 清空角色的数据权限
     */
    Role clearDataPermissions(String roleId);
    
    /**
     * 根据ID查找角色
     */
    Optional<Role> findById(String roleId);
    
    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByCode(RoleCode code);
    
    /**
     * 查找所有角色
     */
    List<Role> findAll();
    
    /**
     * 查找启用的角色
     */
    List<Role> findAllEnabled();
    
    /**
     * 根据ID集合查找角色
     */
    Set<Role> findByIds(Set<String> ids);
    
    /**
     * 删除角色
     */
    void deleteRole(String roleId);
    
    /**
     * 检查角色编码是否存在
     */
    boolean isCodeExists(RoleCode code);
    
    /**
     * 检查角色编码是否已被其他角色使用
     */
    boolean isCodeUsedByOtherRole(String roleId, RoleCode code);
}

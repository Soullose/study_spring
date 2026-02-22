package com.wsf.api.service;

import com.wsf.api.dto.role.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色服务接口
 */
public interface RoleService {
    
    /**
     * 创建角色
     */
    RoleDto createRole(CreateRoleRequest request);
    
    /**
     * 更新角色
     */
    RoleDto updateRole(String roleId, UpdateRoleRequest request);
    
    /**
     * 根据ID查找角色
     */
    Optional<RoleDto> findById(String roleId);
    
    /**
     * 查找所有角色
     */
    List<RoleDto> findAll();
    
    /**
     * 查找启用的角色
     */
    List<RoleDto> findAllEnabled();
    
    /**
     * 删除角色
     */
    void deleteRole(String roleId);
    
    /**
     * 启用角色
     */
    RoleDto enableRole(String roleId);
    
    /**
     * 禁用角色
     */
    RoleDto disableRole(String roleId);
    
    /**
     * 分配菜单给角色
     */
    RoleDto assignMenus(String roleId, Set<String> menuIds);
    
    /**
     * 分配数据权限给角色
     */
    RoleDto assignDataPermissions(String roleId, Set<String> permissionIds);
}

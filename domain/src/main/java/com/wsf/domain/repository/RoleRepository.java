package com.wsf.domain.repository;

import com.wsf.domain.model.role.aggregate.Role;
import com.wsf.domain.model.role.valueobject.RoleCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色仓储接口
 */
public interface RoleRepository {
    
    /**
     * 保存角色
     */
    Role save(Role role);
    
    /**
     * 批量保存角色
     */
    List<Role> saveAll(List<Role> roles);
    
    /**
     * 根据ID查找角色
     */
    Optional<Role> findById(String id);
    
    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByCode(RoleCode code);
    
    /**
     * 查找所有角色
     */
    List<Role> findAll();
    
    /**
     * 根据ID列表查找角色
     */
    List<Role> findByIds(List<String> ids);
    
    /**
     * 根据ID集合查找角色
     */
    Set<Role> findByIds(Set<String> ids);
    
    /**
     * 查找启用的角色
     */
    List<Role> findAllEnabled();
    
    /**
     * 删除角色
     */
    void deleteById(String id);
    
    /**
     * 检查角色编码是否存在
     */
    boolean existsByCode(RoleCode code);
    
    /**
     * 查找角色及其关联的菜单
     */
    Optional<Role> findByIdWithMenus(String id);
    
    /**
     * 查找角色及其关联的数据权限
     */
    Optional<Role> findByIdWithDataPermissions(String id);
    
    /**
     * 查找角色的完整信息（包含所有关联）
     */
    Optional<Role> findByIdWithAllRelations(String id);
}

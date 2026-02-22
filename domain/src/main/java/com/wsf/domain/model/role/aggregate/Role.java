package com.wsf.domain.model.role.aggregate;

import com.wsf.domain.model.role.valueobject.*;
import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.datapermission.entity.DataPermission;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Role聚合根
 * 表示角色信息
 */
@Getter
public class Role {
    
    /**
     * 角色ID
     */
    private final String id;
    
    /**
     * 角色编码
     */
    private final RoleCode code;
    
    /**
     * 角色名称
     */
    private RoleName name;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private boolean enabled;
    
    /**
     * 关联的菜单列表
     */
    private final Set<Menu> menus = new HashSet<>();
    
    /**
     * 关联的数据权限列表
     */
    private final Set<DataPermission> dataPermissions = new HashSet<>();
    
    /**
     * 创建时间
     */
    private final LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 私有构造函数
     */
    private Role(String id, RoleCode code, RoleName name, String description, boolean enabled) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }
    
    /**
     * 创建角色（工厂方法）
     */
    public static Role create(String id, RoleCode code, RoleName name, String description) {
        return new Role(id, code, name, description, true);
    }
    
    /**
     * 重建角色（从持久化层恢复）
     */
    public static Role rebuild(String id, RoleCode code, RoleName name, String description,
                               boolean enabled, LocalDateTime createTime, LocalDateTime updateTime) {
        Role role = new Role(id, code, name, description, enabled);
        try {
            var field = Role.class.getDeclaredField("createTime");
            field.setAccessible(true);
            field.set(role, createTime);
        } catch (Exception ignored) {
        }
        role.updateTime = updateTime;
        return role;
    }
    
    /**
     * 更新角色信息
     */
    public void update(RoleName name, String description) {
        if (name != null) {
            this.name = name;
        }
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 启用角色
     */
    public void enable() {
        this.enabled = true;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 禁用角色
     */
    public void disable() {
        this.enabled = false;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 分配菜单
     */
    public void assignMenu(Menu menu) {
        this.menus.add(menu);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 移除菜单
     */
    public void removeMenu(String menuId) {
        this.menus.removeIf(menu -> menu.getId().equals(menuId));
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 批量分配菜单
     */
    public void assignMenus(Set<Menu> menus) {
        this.menus.addAll(menus);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 清空菜单
     */
    public void clearMenus() {
        this.menus.clear();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 分配数据权限
     */
    public void assignDataPermission(DataPermission permission) {
        this.dataPermissions.add(permission);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 移除数据权限
     */
    public void removeDataPermission(String permissionId) {
        this.dataPermissions.removeIf(p -> p.getId().equals(permissionId));
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 批量分配数据权限
     */
    public void assignDataPermissions(Set<DataPermission> permissions) {
        this.dataPermissions.addAll(permissions);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 清空数据权限
     */
    public void clearDataPermissions() {
        this.dataPermissions.clear();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 获取菜单ID列表
     */
    public Set<String> getMenuIds() {
        return menus.stream().map(Menu::getId).collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * 获取数据权限ID列表
     */
    public Set<String> getDataPermissionIds() {
        return dataPermissions.stream()
            .map(DataPermission::getId)
            .collect(java.util.stream.Collectors.toSet());
    }
}

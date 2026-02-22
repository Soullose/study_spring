package com.wsf.domain.model.permission.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Permission实体
 * 表示权限信息
 */
@Getter
public class Permission {
    
    /**
     * 权限ID
     */
    private final String id;
    
    /**
     * 权限编码
     */
    private final String code;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 资源标识
     */
    private String resource;
    
    /**
     * 操作类型
     */
    private String action;
    
    /**
     * 关联菜单ID
     */
    private String menuId;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private boolean enabled;
    
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
    private Permission(String id, String code, String name, String resource,
                       String action, String menuId, String description, boolean enabled) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.menuId = menuId;
        this.description = description;
        this.enabled = enabled;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }
    
    /**
     * 创建权限（工厂方法）
     */
    public static Permission create(String id, String code, String name, 
                                    String resource, String action, String menuId, String description) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Permission code cannot be empty");
        }
        return new Permission(id, code, name, resource, action, menuId, description, true);
    }
    
    /**
     * 重建权限（从持久化层恢复）
     */
    public static Permission rebuild(String id, String code, String name, String resource,
                                     String action, String menuId, String description, boolean enabled,
                                     LocalDateTime createTime, LocalDateTime updateTime) {
        Permission permission = new Permission(id, code, name, resource, action, menuId, description, enabled);
        try {
            var field = Permission.class.getDeclaredField("createTime");
            field.setAccessible(true);
            field.set(permission, createTime);
        } catch (Exception ignored) {
        }
        permission.updateTime = updateTime;
        return permission;
    }
    
    /**
     * 更新权限信息
     */
    public void update(String name, String resource, String action, String description) {
        this.name = name;
        this.resource = resource;
        this.action = action;
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 关联菜单
     */
    public void linkMenu(String menuId) {
        this.menuId = menuId;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 启用权限
     */
    public void enable() {
        this.enabled = true;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 禁用权限
     */
    public void disable() {
        this.enabled = false;
        this.updateTime = LocalDateTime.now();
    }
}

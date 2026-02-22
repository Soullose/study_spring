package com.wsf.domain.model.datapermission.entity;

import com.wsf.domain.model.datapermission.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DataPermission实体
 * 表示数据权限信息
 */
@Getter
public class DataPermission {
    
    /**
     * 数据权限ID
     */
    private final String id;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 资源类型
     */
    private ResourceType resourceType;
    
    /**
     * 数据范围
     */
    private DataScope dataScope;
    
    /**
     * 自定义资源ID列表（逗号分隔）
     */
    private String resourceIds;
    
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
    private DataPermission(String id, String name, ResourceType resourceType,
                           DataScope dataScope, String resourceIds, String description, boolean enabled) {
        this.id = id;
        this.name = name;
        this.resourceType = resourceType;
        this.dataScope = dataScope;
        this.resourceIds = resourceIds;
        this.description = description;
        this.enabled = enabled;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }
    
    /**
     * 创建数据权限（工厂方法）
     */
    public static DataPermission create(String id, String name, ResourceType resourceType,
                                        DataScope dataScope, String description) {
        return new DataPermission(id, name, resourceType, dataScope, null, description, true);
    }
    
    /**
     * 创建自定义数据权限
     */
    public static DataPermission createCustom(String id, String name, ResourceType resourceType,
                                              Set<String> resourceIds, String description) {
        String ids = resourceIds != null ? String.join(",", resourceIds) : null;
        return new DataPermission(id, name, resourceType, DataScope.CUSTOM, ids, description, true);
    }
    
    /**
     * 重建数据权限（从持久化层恢复）
     */
    public static DataPermission rebuild(String id, String name, ResourceType resourceType,
                                         DataScope dataScope, String resourceIds, String description,
                                         boolean enabled, LocalDateTime createTime, LocalDateTime updateTime) {
        DataPermission permission = new DataPermission(id, name, resourceType, dataScope, resourceIds, description, enabled);
        try {
            var field = DataPermission.class.getDeclaredField("createTime");
            field.setAccessible(true);
            field.set(permission, createTime);
        } catch (Exception ignored) {
        }
        permission.updateTime = updateTime;
        return permission;
    }
    
    /**
     * 更新数据权限信息
     */
    public void update(String name, DataScope dataScope, String description) {
        this.name = name;
        this.dataScope = dataScope;
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新自定义资源ID列表
     */
    public void updateResourceIds(Set<String> resourceIds) {
        this.resourceIds = resourceIds != null ? String.join(",", resourceIds) : null;
        this.dataScope = DataScope.CUSTOM;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 获取资源ID集合
     */
    public Set<String> getResourceIdSet() {
        if (resourceIds == null || resourceIds.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(resourceIds.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
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
    
    /**
     * 是否为自定义范围
     */
    public boolean isCustom() {
        return dataScope == DataScope.CUSTOM;
    }
    
    /**
     * 是否为全部数据权限
     */
    public boolean isAll() {
        return dataScope == DataScope.ALL;
    }
}

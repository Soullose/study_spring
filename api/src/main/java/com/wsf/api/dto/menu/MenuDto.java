package com.wsf.api.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {
    
    /**
     * 菜单ID
     */
    private String id;
    
    /**
     * 菜单名称
     */
    private String name;
    
    /**
     * 父菜单ID
     */
    private String parentId;
    
    /**
     * 菜单类型: DIR/MENU/BUTTON
     */
    private String menuType;
    
    /**
     * 路由路径
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 权限标识
     */
    private String permission;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 是否显示
     */
    private Boolean visible;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 外链地址
     */
    private String externalLink;
    
    /**
     * 是否缓存
     */
    private Boolean cacheEnabled;
    
    /**
     * 子菜单列表
     */
    private List<MenuDto> children;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

package com.wsf.api.dto.menu;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建菜单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {
    
    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String name;
    
    /**
     * 父菜单ID
     */
    private String parentId;
    
    /**
     * 菜单类型: DIR/MENU/BUTTON
     */
    @NotBlank(message = "菜单类型不能为空")
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
     * 外链地址
     */
    private String externalLink;
    
    /**
     * 是否缓存
     */
    private Boolean cacheEnabled;
}

package com.wsf.api.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新菜单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuRequest {
    
    /**
     * 菜单名称
     */
    private String name;
    
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

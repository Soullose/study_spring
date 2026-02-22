package com.wsf.domain.model.menu.aggregate;

import com.wsf.domain.model.menu.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu聚合根
 * 表示菜单信息，支持树形结构
 */
@Getter
public class Menu {
    
    /**
     * 菜单ID
     */
    private final String id;
    
    /**
     * 菜单名称
     */
    private String name;
    
    /**
     * 父菜单ID
     */
    private String parentId;
    
    /**
     * 菜单类型
     */
    private MenuType menuType;
    
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
     * 菜单状态
     */
    private MenuStatus status;
    
    /**
     * 外链地址
     */
    private String externalLink;
    
    /**
     * 是否缓存
     */
    private boolean cacheEnabled;
    
    /**
     * 子菜单列表
     */
    private final List<Menu> children = new ArrayList<>();
    
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
    private Menu(String id, String name, String parentId, MenuType menuType,
                 String path, String component, String permission, String icon,
                 Integer sortOrder, MenuStatus status, String externalLink, boolean cacheEnabled) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.menuType = menuType;
        this.path = path;
        this.component = component;
        this.permission = permission;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.status = status;
        this.externalLink = externalLink;
        this.cacheEnabled = cacheEnabled;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }
    
    /**
     * 创建目录菜单
     */
    public static Menu createDirectory(String id, String name, String parentId, 
                                       String icon, Integer sortOrder) {
        return new Menu(id, name, parentId, MenuType.DIR, null, null, 
                       null, icon, sortOrder, MenuStatus.normal(), null, false);
    }
    
    /**
     * 创建页面菜单
     */
    public static Menu createMenu(String id, String name, String parentId,
                                  String path, String component, String permission,
                                  String icon, Integer sortOrder) {
        return new Menu(id, name, parentId, MenuType.MENU, path, component,
                       permission, icon, sortOrder, MenuStatus.normal(), null, false);
    }
    
    /**
     * 创建按钮
     */
    public static Menu createButton(String id, String name, String parentId,
                                    String permission, Integer sortOrder) {
        return new Menu(id, name, parentId, MenuType.BUTTON, null, null,
                       permission, null, sortOrder, MenuStatus.normal(), null, false);
    }
    
    /**
     * 重建菜单（从持久化层恢复）
     */
    public static Menu rebuild(String id, String name, String parentId, MenuType menuType,
                               String path, String component, String permission, String icon,
                               Integer sortOrder, MenuStatus status, String externalLink,
                               boolean cacheEnabled, LocalDateTime createTime, LocalDateTime updateTime) {
        return new Menu(id, name, parentId, menuType, path, component, permission,
                       icon, sortOrder, status, externalLink, cacheEnabled);
    }
    
    /**
     * 更新菜单信息
     */
    public void update(String name, String path, String component, 
                       String permission, String icon, Integer sortOrder) {
        this.name = name;
        this.path = path;
        this.component = component;
        this.permission = permission;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新状态
     */
    public void updateStatus(MenuStatus status) {
        this.status = status;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 显示菜单
     */
    public void show() {
        this.status = status.show();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 隐藏菜单
     */
    public void hide() {
        this.status = status.hide();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 启用菜单
     */
    public void enable() {
        this.status = status.enable();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 禁用菜单
     */
    public void disable() {
        this.status = status.disable();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 添加子菜单
     */
    public void addChild(Menu child) {
        this.children.add(child);
    }
    
    /**
     * 移除子菜单
     */
    public void removeChild(String childId) {
        this.children.removeIf(child -> child.getId().equals(childId));
    }
    
    /**
     * 是否为根菜单
     */
    public boolean isRoot() {
        return parentId == null || parentId.isBlank();
    }
    
    /**
     * 是否为目录
     */
    public boolean isDirectory() {
        return menuType == MenuType.DIR;
    }
    
    /**
     * 是否为菜单
     */
    public boolean isMenu() {
        return menuType == MenuType.MENU;
    }
    
    /**
     * 是否为按钮
     */
    public boolean isButton() {
        return menuType == MenuType.BUTTON;
    }
    
    /**
     * 是否可用
     */
    public boolean isAvailable() {
        return status.isAvailable();
    }
    
    /**
     * 是否显示
     */
    public boolean isVisible() {
        return status.isVisible();
    }
}

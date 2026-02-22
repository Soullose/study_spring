package com.wsf.domain.service;

import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.menu.valueobject.MenuType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 菜单领域服务
 * 处理菜单相关的领域逻辑
 */
public interface MenuDomainService {
    
    /**
     * 创建目录菜单
     */
    Menu createDirectory(String name, String parentId, String icon, Integer sortOrder);
    
    /**
     * 创建页面菜单
     */
    Menu createMenu(String name, String parentId, String path, String component, 
                    String permission, String icon, Integer sortOrder);
    
    /**
     * 创建按钮
     */
    Menu createButton(String name, String parentId, String permission, Integer sortOrder);
    
    /**
     * 更新菜单
     */
    Menu updateMenu(String menuId, String name, String path, String component,
                    String permission, String icon, Integer sortOrder);
    
    /**
     * 显示菜单
     */
    Menu showMenu(String menuId);
    
    /**
     * 隐藏菜单
     */
    Menu hideMenu(String menuId);
    
    /**
     * 启用菜单
     */
    Menu enableMenu(String menuId);
    
    /**
     * 禁用菜单
     */
    Menu disableMenu(String menuId);
    
    /**
     * 根据ID查找菜单
     */
    Optional<Menu> findById(String menuId);
    
    /**
     * 查找所有菜单
     */
    List<Menu> findAll();
    
    /**
     * 查找启用的菜单
     */
    List<Menu> findAllEnabled();
    
    /**
     * 查找根菜单
     */
    List<Menu> findRootMenus();
    
    /**
     * 根据父菜单ID查找子菜单
     */
    List<Menu> findByParentId(String parentId);
    
    /**
     * 根据菜单类型查找菜单
     */
    List<Menu> findByMenuType(MenuType menuType);
    
    /**
     * 根据ID集合查找菜单
     */
    Set<Menu> findByIds(Set<String> ids);
    
    /**
     * 构建菜单树
     */
    List<Menu> buildMenuTree();
    
    /**
     * 构建指定菜单的树
     */
    List<Menu> buildMenuTree(List<Menu> menus);
    
    /**
     * 删除菜单
     */
    void deleteMenu(String menuId);
    
    /**
     * 检查菜单是否有子菜单
     */
    boolean hasChildren(String menuId);
    
    /**
     * 检查菜单名称是否存在
     */
    boolean isNameExists(String name);
}

package com.wsf.domain.repository;

import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.menu.valueobject.MenuType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 菜单仓储接口
 */
public interface MenuRepository {
    
    /**
     * 保存菜单
     */
    Menu save(Menu menu);
    
    /**
     * 批量保存菜单
     */
    List<Menu> saveAll(List<Menu> menus);
    
    /**
     * 根据ID查找菜单
     */
    Optional<Menu> findById(String id);
    
    /**
     * 查找所有菜单
     */
    List<Menu> findAll();
    
    /**
     * 根据ID列表查找菜单
     */
    List<Menu> findByIds(List<String> ids);
    
    /**
     * 根据ID集合查找菜单
     */
    Set<Menu> findByIds(Set<String> ids);
    
    /**
     * 查找根菜单（无父菜单）
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
     * 查找启用的菜单
     */
    List<Menu> findAllEnabled();
    
    /**
     * 查找启用的菜单（按排序号排序）
     */
    List<Menu> findAllEnabledOrderBySortOrder();
    
    /**
     * 构建菜单树
     */
    List<Menu> buildMenuTree(List<Menu> menus);
    
    /**
     * 删除菜单
     */
    void deleteById(String id);
    
    /**
     * 检查菜单名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查菜单是否有子菜单
     */
    boolean hasChildren(String menuId);
}

package com.wsf.api.service;

import com.wsf.api.dto.menu.*;

import java.util.List;
import java.util.Optional;

/**
 * 菜单服务接口
 */
public interface MenuService {
    
    /**
     * 创建菜单
     */
    MenuDto createMenu(CreateMenuRequest request);
    
    /**
     * 更新菜单
     */
    MenuDto updateMenu(String menuId, UpdateMenuRequest request);
    
    /**
     * 根据ID查找菜单
     */
    Optional<MenuDto> findById(String menuId);
    
    /**
     * 查找所有菜单
     */
    List<MenuDto> findAll();
    
    /**
     * 查找启用的菜单
     */
    List<MenuDto> findAllEnabled();
    
    /**
     * 获取菜单树
     */
    List<MenuDto> getMenuTree();
    
    /**
     * 删除菜单
     */
    void deleteMenu(String menuId);
    
    /**
     * 显示菜单
     */
    MenuDto showMenu(String menuId);
    
    /**
     * 隐藏菜单
     */
    MenuDto hideMenu(String menuId);
    
    /**
     * 启用菜单
     */
    MenuDto enableMenu(String menuId);
    
    /**
     * 禁用菜单
     */
    MenuDto disableMenu(String menuId);
}

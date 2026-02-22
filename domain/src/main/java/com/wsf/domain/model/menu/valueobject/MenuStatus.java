package com.wsf.domain.model.menu.valueobject;

/**
 * 菜单状态值对象
 */
public record MenuStatus(boolean visible, boolean enabled) {
    
    /**
     * 正常显示状态
     */
    public static MenuStatus normal() {
        return new MenuStatus(true, true);
    }
    
    /**
     * 隐藏状态
     */
    public static MenuStatus hidden() {
        return new MenuStatus(false, true);
    }
    
    /**
     * 禁用状态
     */
    public static MenuStatus disabled() {
        return new MenuStatus(true, false);
    }
    
    /**
     * 隐藏且禁用状态
     */
    public static MenuStatus hiddenAndDisabled() {
        return new MenuStatus(false, false);
    }
    
    /**
     * 是否可用
     */
    public boolean isAvailable() {
        return enabled;
    }
    
    /**
     * 是否显示
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * 显示菜单
     */
    public MenuStatus show() {
        return new MenuStatus(true, enabled);
    }
    
    /**
     * 隐藏菜单
     */
    public MenuStatus hide() {
        return new MenuStatus(false, enabled);
    }
    
    /**
     * 启用菜单
     */
    public MenuStatus enable() {
        return new MenuStatus(visible, true);
    }
    
    /**
     * 禁用菜单
     */
    public MenuStatus disable() {
        return new MenuStatus(visible, false);
    }
}

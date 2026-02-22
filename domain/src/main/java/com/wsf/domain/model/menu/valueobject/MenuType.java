package com.wsf.domain.model.menu.valueobject;

/**
 * 菜单类型枚举
 */
public enum MenuType {
    /**
     * 目录
     */
    DIR("目录"),
    /**
     * 菜单
     */
    MENU("菜单"),
    /**
     * 按钮
     */
    BUTTON("按钮");
    
    private final String description;
    
    MenuType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

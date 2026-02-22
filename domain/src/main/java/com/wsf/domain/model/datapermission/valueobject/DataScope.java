package com.wsf.domain.model.datapermission.valueobject;

/**
 * 数据范围枚举
 * 定义数据权限的范围级别
 */
public enum DataScope {
    /**
     * 全部数据
     */
    ALL("全部数据", 1),
    /**
     * 本部门及下级部门数据
     */
    DEPT_AND_BELOW("本部门及下级部门", 2),
    /**
     * 本部门数据
     */
    DEPT("本部门", 3),
    /**
     * 仅本人数据
     */
    SELF("仅本人", 4),
    /**
     * 自定义（指定部门/组织）
     */
    CUSTOM("自定义", 5);
    
    private final String description;
    private final int level;
    
    DataScope(String description, int level) {
        this.description = description;
        this.level = level;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * 判断是否包含指定范围
     * 级别越小，权限越大
     */
    public boolean includes(DataScope other) {
        return this.level <= other.level;
    }
}

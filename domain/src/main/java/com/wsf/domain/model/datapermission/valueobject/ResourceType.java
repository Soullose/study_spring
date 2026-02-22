package com.wsf.domain.model.datapermission.valueobject;

/**
 * 资源类型枚举
 * 定义数据权限关联的资源类型
 */
public enum ResourceType {
    /**
     * 部门
     */
    DEPT("部门"),
    /**
     * 组织
     */
    ORG("组织"),
    /**
     * 自定义
     */
    CUSTOM("自定义");
    
    private final String description;
    
    ResourceType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

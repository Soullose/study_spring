package com.wsf.domain.model.role.valueobject;

import org.springframework.util.StringUtils;

/**
 * 角色名称值对象
 */
public record RoleName(String value) {
    
    public RoleName {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("Role name cannot exceed 50 characters");
        }
    }
    
    public static RoleName of(String value) {
        return new RoleName(value);
    }
}

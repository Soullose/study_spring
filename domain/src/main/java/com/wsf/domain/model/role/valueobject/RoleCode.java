package com.wsf.domain.model.role.valueobject;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 角色编码值对象
 */
public record RoleCode(String value) {
    
    private static final Pattern ROLE_CODE_PATTERN = 
        Pattern.compile("^[A-Z][A-Z0-9_]*$");
    
    public RoleCode {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Role code cannot be empty");
        }
        if (!ROLE_CODE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Invalid role code format: " + value + ". Must start with uppercase letter and contain only uppercase letters, numbers and underscores"
            );
        }
    }
    
    public static RoleCode of(String value) {
        return new RoleCode(value);
    }
}

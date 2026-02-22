package com.wsf.domain.model.user.valueobject;

import java.util.regex.Pattern;

/**
 * 邮箱值对象
 * 包含格式验证逻辑
 */
public record Email(String value) {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    public Email {
        if (value != null && !value.isBlank() && !EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
    
    public boolean isEmpty() {
        return value == null || value.isBlank();
    }
}

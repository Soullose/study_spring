package com.wsf.domain.model.user.valueobject;

import java.util.regex.Pattern;

/**
 * 手机号值对象
 * 包含格式验证逻辑
 */
public record PhoneNumber(String value) {
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^1[3-9]\\d{9}$");
    
    public PhoneNumber {
        if (value != null && !value.isBlank() && !PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + value);
        }
    }
    
    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }
    
    public boolean isEmpty() {
        return value == null || value.isBlank();
    }
}

package com.wsf.domain.model.account.valueobject;

import org.springframework.util.StringUtils;

/**
 * 密码值对象
 * 包含密码验证逻辑
 */
public record Password(String value, boolean encoded) {
    
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 20;
    
    public Password {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        // 如果不是已编码的密码，则进行格式验证
        if (!encoded) {
            if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) {
                throw new IllegalArgumentException(
                    "Password length must be between " + MIN_LENGTH + " and " + MAX_LENGTH
                );
            }
        }
    }
    
    /**
     * 创建原始密码（未编码）
     */
    public static Password ofRaw(String rawPassword) {
        return new Password(rawPassword, false);
    }
    
    /**
     * 创建已编码密码
     */
    public static Password ofEncoded(String encodedPassword) {
        return new Password(encodedPassword, true);
    }
    
    /**
     * 是否已编码
     */
    public boolean isEncoded() {
        return encoded;
    }
    
    /**
     * 获取密码值
     */
    public String getValue() {
        return value;
    }
}

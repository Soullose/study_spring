package com.wsf.domain.model.user.valueobject;

import org.springframework.util.StringUtils;

/**
 * 用户姓名值对象
 * 包含姓和名
 */
public record UserName(String firstName, String lastName) {
    
    public UserName {
        if (!StringUtils.hasText(firstName) && !StringUtils.hasText(lastName)) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
    }
    
    public static UserName of(String firstName, String lastName) {
        return new UserName(firstName, lastName);
    }
    
    public static UserName ofFullName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        // 简单处理：假设第一个字是姓，其余是名
        if (fullName.length() == 1) {
            return new UserName(fullName, "");
        }
        return new UserName(fullName.substring(0, 1), fullName.substring(1));
    }
    
    /**
     * 获取全名
     */
    public String getFullName() {
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + lastName;
    }
    
    /**
     * 获取显示名称（姓+名的首字母）
     */
    public String getDisplayName() {
        if (!StringUtils.hasText(lastName)) {
            return firstName;
        }
        return firstName + lastName.charAt(0);
    }
}

package com.wsf.domain.model.user.valueobject;

import java.util.regex.Pattern;

/**
 * 身份证号值对象
 * 包含格式验证逻辑（支持18位身份证）
 */
public record IdCardNumber(String value) {
    
    private static final Pattern ID_CARD_PATTERN = 
        Pattern.compile("^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");
    
    public IdCardNumber {
        if (value != null && !value.isBlank() && !ID_CARD_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid ID card number format: " + value);
        }
    }
    
    public static IdCardNumber of(String value) {
        return new IdCardNumber(value);
    }
    
    public boolean isEmpty() {
        return value == null || value.isBlank();
    }
    
    /**
     * 获取出生日期
     */
    public String getBirthDate() {
        if (isEmpty() || value.length() < 14) {
            return null;
        }
        return value.substring(6, 10) + "-" + value.substring(10, 12) + "-" + value.substring(12, 14);
    }
    
    /**
     * 获取性别（1-男，2-女）
     */
    public Integer getGender() {
        if (isEmpty() || value.length() < 17) {
            return null;
        }
        int genderCode = Integer.parseInt(value.substring(16, 17));
        return genderCode % 2 == 1 ? 1 : 2;
    }
}

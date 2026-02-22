package com.wsf.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    /**
     * 用户ID
     */
    private String id;
    
    /**
     * 名
     */
    private String firstName;
    
    /**
     * 姓
     */
    private String lastName;
    
    /**
     * 全名
     */
    private String fullName;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phoneNumber;
    
    /**
     * 身份证号
     */
    private String idCardNumber;
    
    /**
     * 是否有关联账户
     */
    private Boolean hasAccount;
    
    /**
     * 账户ID
     */
    private String accountId;
    
    /**
     * 账户用户名
     */
    private String username;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

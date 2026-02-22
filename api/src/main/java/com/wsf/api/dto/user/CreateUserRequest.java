package com.wsf.api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建用户请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    /**
     * 名
     */
    private String firstName;
    
    /**
     * 姓
     */
    private String lastName;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
    
    /**
     * 身份证号
     */
    private String idCardNumber;
    
    /**
     * 是否同时创建账户
     */
    private Boolean createAccount;
    
    /**
     * 账户用户名（如果createAccount为true则必填）
     */
    private String username;
    
    /**
     * 账户密码（如果createAccount为true则必填）
     */
    private String password;
}

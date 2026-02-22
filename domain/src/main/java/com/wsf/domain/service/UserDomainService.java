package com.wsf.domain.service;

import com.wsf.domain.model.user.aggregate.User;
import com.wsf.domain.model.user.valueobject.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户领域服务
 * 处理用户相关的领域逻辑
 */
public interface UserDomainService {
    
    /**
     * 创建用户
     */
    User createUser(UserName name, Email email, PhoneNumber phoneNumber, IdCardNumber idCardNumber);
    
    /**
     * 更新用户资料
     */
    User updateUserProfile(String userId, UserName name, Email email, PhoneNumber phoneNumber);
    
    /**
     * 更新用户身份证号
     */
    User updateUserIdCard(String userId, IdCardNumber idCardNumber);
    
    /**
     * 根据ID查找用户
     */
    Optional<User> findById(String userId);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhoneNumber(PhoneNumber phoneNumber);
    
    /**
     * 查找所有用户
     */
    List<User> findAll();
    
    /**
     * 删除用户
     */
    void deleteUser(String userId);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExists(Email email);
    
    /**
     * 检查手机号是否已存在
     */
    boolean isPhoneNumberExists(PhoneNumber phoneNumber);
    
    /**
     * 检查邮箱是否已被其他用户使用
     */
    boolean isEmailUsedByOtherUser(String userId, Email email);
    
    /**
     * 检查手机号是否已被其他用户使用
     */
    boolean isPhoneNumberUsedByOtherUser(String userId, PhoneNumber phoneNumber);
}

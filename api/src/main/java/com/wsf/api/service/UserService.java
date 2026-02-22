package com.wsf.api.service;

import com.wsf.api.dto.user.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 创建用户
     */
    UserDto createUser(CreateUserRequest request);
    
    /**
     * 更新用户
     */
    UserDto updateUser(String userId, UpdateUserRequest request);
    
    /**
     * 根据ID查找用户
     */
    Optional<UserDto> findById(String userId);
    
    /**
     * 查找所有用户
     */
    List<UserDto> findAll();
    
    /**
     * 删除用户
     */
    void deleteUser(String userId);
    
    /**
     * 为用户创建账户
     */
    UserDto createAccountForUser(String userId, String username, String password);
    
    /**
     * 解除用户与账户的关联
     */
    UserDto unlinkAccount(String userId);
}

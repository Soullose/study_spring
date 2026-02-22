package com.wsf.domain.repository;

import com.wsf.domain.model.user.aggregate.User;
import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.PhoneNumber;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {
    
    /**
     * 保存用户
     */
    User save(User user);
    
    /**
     * 批量保存用户
     */
    List<User> saveAll(List<User> users);
    
    /**
     * 根据ID查找用户
     */
    Optional<User> findById(String id);
    
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
     * 根据ID列表查找用户
     */
    List<User> findByIds(List<String> ids);
    
    /**
     * 删除用户
     */
    void deleteById(String id);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(Email email);
    
    /**
     * 检查手机号是否存在
     */
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
}

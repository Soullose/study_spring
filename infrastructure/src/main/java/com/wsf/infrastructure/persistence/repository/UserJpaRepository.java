package com.wsf.infrastructure.persistence.repository;

import com.wsf.infrastructure.persistence.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户JPA仓储
 */
@Repository
public interface UserJpaRepository 
        extends JpaRepository<User, String>, 
                JpaSpecificationExecutor<User>,
                QuerydslPredicateExecutor<User> {
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    /**
     * 根据身份证号查找用户
     */
    Optional<User> findByIdCardNumber(String idCardNumber);
    
    /**
     * 根据真实姓名模糊查询用户列表
     */
    List<User> findByRealNameContaining(String realName);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查手机号是否存在
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * 检查身份证号是否存在
     */
    boolean existsByIdCardNumber(String idCardNumber);
}

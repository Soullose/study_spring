package com.wsf.domain.model.user.aggregate;

import com.wsf.domain.model.user.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * User聚合根
 * 表示用户基本信息，可以独立存在，不一定需要账户
 */
@Getter
public class User {
    
    /**
     * 用户ID
     */
    private final String id;
    
    /**
     * 用户姓名
     */
    private UserName name;
    
    /**
     * 邮箱
     */
    private Email email;
    
    /**
     * 手机号
     */
    private PhoneNumber phoneNumber;
    
    /**
     * 身份证号
     */
    private IdCardNumber idCardNumber;
    
    /**
     * 真实姓名（冗余字段，方便查询）
     */
    private String realName;
    
    /**
     * 创建时间
     */
    private final LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建用户
     */
    private User(String id, UserName name, Email email, PhoneNumber phoneNumber, 
                 IdCardNumber idCardNumber, String realName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.idCardNumber = idCardNumber;
        this.realName = realName;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }
    
    /**
     * 创建用户（工厂方法）
     */
    public static User create(String id, UserName name, Email email, 
                              PhoneNumber phoneNumber, IdCardNumber idCardNumber) {
        String realName = name != null ? name.getFullName() : null;
        return new User(id, name, email, phoneNumber, idCardNumber, realName);
    }
    
    /**
     * 重建用户（从持久化层恢复）
     */
    public static User rebuild(String id, UserName name, Email email, 
                               PhoneNumber phoneNumber, IdCardNumber idCardNumber,
                               String realName, LocalDateTime createTime, LocalDateTime updateTime) {
        User user = new User(id, name, email, phoneNumber, idCardNumber, realName);
        // 使用反射设置final字段 createTime
        try {
            var field = User.class.getDeclaredField("createTime");
            field.setAccessible(true);
            field.set(user, createTime);
        } catch (Exception ignored) {
        }
        user.updateTime = updateTime;
        return user;
    }
    
    /**
     * 更新用户资料
     */
    public void updateProfile(UserName name, Email email, PhoneNumber phoneNumber) {
        if (name != null) {
            this.name = name;
            this.realName = name.getFullName();
        }
        if (email != null) {
            this.email = email;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新身份证号
     */
    public void updateIdCardNumber(IdCardNumber idCardNumber) {
        this.idCardNumber = idCardNumber;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 获取全名
     */
    public String getFullName() {
        return name != null ? name.getFullName() : realName;
    }
}

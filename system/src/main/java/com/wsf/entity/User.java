package com.wsf.entity;

/**
 * open
 * SoulLose
 * 2022-04-28 09:44
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@Table(name = "t_open_user_")
public class User implements Serializable {
    
    private static final long serialVersionUID = 3050292059722106684L;
    
    /**
     * 主键
     */
    @Id
    @GenericGenerator(name = "idGenerator", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "idGenerator")
    @Column(name = "id_")
    @Access(AccessType.PROPERTY)
    private String id;
    /**
     * 用户名
     */
    @Column(name = "user_name_")
    private String userName;
    /**
     * 昵称
     */
    @Column(name = "nick_name_")
    private String nickName;
    /**
     * 密码
     */
    @Column(name = "password_")
    private String password;
    /**
     * 账号状态（0正常 1停用）
     */
    @Column(name = "status_")
    private String status;
    /**
     * 邮箱
     */
    @Column(name = "email_")
    private String email;
    /**
     * 手机号
     */
    @Column(name = "phone_number_")
    private String phoneNumber;
    /**
     * 用户性别（0男，1女，2未知）
     */
    @Column(name = "sex_")
    private String sex;
    /**
     * 头像
     */
    @Column(name = "avatar_")
    private String avatar;
    /**
     * 用户类型（0管理员，1普通用户）
     */
    @Column(name = "user_type_")
    private String userType;
    /**
     * 创建人的用户id
     */
    @Column(name = "create_by_")
    private Long createBy;
    /**
     * 创建时间
     */
    @Column(name = "create_time_")
    private Date createTime;
    /**
     * 更新人
     */
    @Column(name = "update_by_")
    private Long updateBy;
    /**
     * 更新时间
     */
    @Column(name = "update_time_")
    private Date updateTime;
    /**
     * 删除标志（0代表未删除，1代表已删除）
     */
    @Column(name = "del_flag_")
    private Integer delFlag;
}

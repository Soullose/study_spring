package com.wsf.entity;

import com.google.common.collect.Sets;
import com.wsf.domain.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_user_account_")
public class UserAccount extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 6402450559307770244L;

    @Column(name = "username_")
    private String username;

    @Column(name = "password_")
    private String password;

    ///帐户未过期
    @Column(name = "account_nonExpired")
    private boolean accountNonExpired;

    ///帐户未锁定
    @Column(name = "account_nonLocked")
    private boolean accountNonLocked;

    ///证书未过期
    @Column(name = "credentials_nonExpired")
    private boolean credentialsNonExpired;

    ///禁用
    @Column(name = "enabled_")
    private boolean enabled;

    @OneToOne(mappedBy = "userAccount")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_open_userAccount_role_",
            joinColumns = {@JoinColumn(name = "userAccount_id_")},
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id_")
            })
    private Set<Role> roles = Sets.newHashSet();
}

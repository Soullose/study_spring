package com.wsf.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import com.wsf.domain.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * open
 * SoulLose
 * 2022-05-09 20:37
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_open_role_")
public class Role extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -5749803185558149450L;

    @Column(name = "role_name_")
    private String name;

    @Column(name = "role_key_")
    private String key;

    @Column(name = "status_")
    private boolean status = false;

    @Column(name = "createTime_")
    private Date createTime;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_open_userAccount_role_",
            joinColumns = {@JoinColumn(name = "role_id_")},
            inverseJoinColumns = {
                    @JoinColumn(name = "userAccount_id_")})
    private Set<UserAccount> userAccounts;

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "t_open_role_menu_", joinColumns = {@JoinColumn(name = "role_id_")},
            inverseJoinColumns = {
                    @JoinColumn(name = "menu_id_")})
    private Set<Menu> menus = Sets.newHashSet();
}

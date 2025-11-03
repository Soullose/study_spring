package com.wsf.infrastructure.security.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wsf.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

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
    private LocalDateTime createTime;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "t_open_userAccount_role_",
            joinColumns = {@JoinColumn(name = "role_id_")},
            inverseJoinColumns = {
                    @JoinColumn(name = "userAccount_id_")})
    private Set<UserAccount> userAccounts;
}
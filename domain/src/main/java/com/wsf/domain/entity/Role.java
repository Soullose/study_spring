package com.wsf.domain.entity;

import com.wsf.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_OPEN_ROLE_")
public class Role extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 6402450559307770244L;

    @Column(name = "role_name_")
    private String roleName;

    @Column(name = "role_code_")
    private String roleCode;

    @ManyToMany(mappedBy = "roles")
    private Set<UserAccount> userAccounts;
}
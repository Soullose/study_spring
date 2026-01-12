package com.wsf.infrastructure.persistence.entity.role;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import org.hibernate.annotations.Comment;

import com.wsf.infrastructure.persistence.entity.BaseEntity;
import com.wsf.infrastructure.persistence.entity.user.UserAccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_OPEN_ROLE_")
@Comment("系统角色表")
public class Role extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6402450559307770244L;

    @Column(name = "role_name_")
    private String name;

    @Column(name = "role_code_")
    private String code;

    @ManyToMany(mappedBy = "roles")
    private Set<UserAccount> userAccounts;
}
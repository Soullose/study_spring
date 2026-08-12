package com.wsf.infrastructure.persistence.entity.user;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.wsf.infrastructure.persistence.entity.BaseEntity;
import com.wsf.infrastructure.persistence.entity.menu.MenuPO;
import com.wsf.infrastructure.persistence.entity.role.Role;
import com.wsf.infrastructure.persistence.entity.token.Token;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/// 用于登录系统的账户
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_USER_ACCOUNT_")
@Comment("系统账户表")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class UserAccountPO extends BaseEntity implements Serializable {

        @Serial
        private static final long serialVersionUID = 6402450559307770244L;

        @Column(name = "username_", unique = true)
        private String username;

        @Column(name = "password_")
        private String password;

        /// 帐户未过期
        @Column(name = "account_nonExpired")
        private boolean accountNonExpired;

        /// 帐户未锁定
        @Column(name = "account_nonLocked")
        private boolean accountNonLocked;

        /// 证书未过期
        @Column(name = "credentials_nonExpired")
        private boolean credentialsNonExpired;

        /// 禁用
        @Builder.Default
        @Column(name = "enabled_")
        private boolean enabled = true;

        /// 人员
        @OneToOne(mappedBy = "userAccount")
        private UserPO user;

        /// 登录的token
        @OneToMany(mappedBy = "userAccount")
        private Set<Token> tokens;

        @ElementCollection(fetch = FetchType.LAZY)
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                        name = "t_open_userAccount_role_", joinColumns = {
                                        @JoinColumn(
                                                        name = "userAccount_id_", foreignKey = @ForeignKey(
                                                                ConstraintMode.NO_CONSTRAINT
                                                        )
                                        )}, inverseJoinColumns = {
                                                        @JoinColumn(
                                                                        name = "role_id_", foreignKey = @ForeignKey(
                                                                                ConstraintMode.NO_CONSTRAINT
                                                                        )
                                                        )
                                        }
        )
        private Set<Role> roles;

        /// 账户级补充菜单（角色菜单之外的额外授权）
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                        name = "t_open_userAccount_menu_", joinColumns = {
                                        @JoinColumn(
                                                        name = "userAccount_id_", foreignKey = @ForeignKey(
                                                                ConstraintMode.NO_CONSTRAINT
                                                        )
                                        )}, inverseJoinColumns = {
                                                        @JoinColumn(
                                                                        name = "menu_id_", foreignKey = @ForeignKey(
                                                                                ConstraintMode.NO_CONSTRAINT
                                                                        )
                                                        )
                                        }
        )
        private Set<MenuPO> supplementaryMenus;
}
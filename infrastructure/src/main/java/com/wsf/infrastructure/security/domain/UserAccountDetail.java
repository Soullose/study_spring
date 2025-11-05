package com.wsf.infrastructure.security.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.wsf.infrastructure.security.entity.Role;
import com.wsf.infrastructure.security.entity.UserAccount;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Data
public class UserAccountDetail implements UserDetails {

    private UserAccount userAccount;

    public UserAccountDetail(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    ///获取角色权限
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = userAccount.getRoles();
        int roleSize = roles != null ? roles.size() : 0;
        log.debug("UserAccountDetail-roles:{}", roleSize);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    ///获取密码
    @Override
    public String getPassword() {
        return userAccount.getPassword();
    }

    ///获取用户名
    @Override
    public String getUsername() {
        return userAccount.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return userAccount.isEnabled();
    }
}
package com.wsf.infrastructure.security.domain;

import com.wsf.entity.Role;
import com.wsf.entity.UserAccount;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

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
        log.debug("UserAccountDetail-userAccount:{}",userAccount);
        Set<Role> roles = userAccount.getRoles();
        log.debug("UserAccountDetail-roles:{}",roles);
        if (roles == null) {
            return null;
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
        return true;
    }
}
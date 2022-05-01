package com.wsf.security.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wsf.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * open
 * SoulLose
 * 2022-04-25 09:14
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
//@AllArgsConstructor
public class LoginUserDetail implements UserDetails {
    
    private User user;
    
    private List<String> permissions;
    
    @JsonIgnore
    private List<SimpleGrantedAuthority> authorities;
    
    public LoginUserDetail() {
    }
    
    public LoginUserDetail(final User user, final List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }
    
    
    /**
     * 权限信息
     *
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) {
            return authorities;
        }
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
    
    /**
     * 密码
     *
     * @return
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    /**
     * 用户名
     *
     * @return
     */
    @Override
    public String getUsername() {
        return user.getUserName();
    }
    
    /**
     * 是否账号过期
     *
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    /**
     * 是否账号被锁定
     *
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    /**
     * 凭证（密码）是否过期
     *
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    /**
     * 是否可用
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}

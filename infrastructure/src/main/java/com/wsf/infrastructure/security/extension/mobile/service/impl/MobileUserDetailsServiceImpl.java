package com.wsf.infrastructure.security.extension.mobile.service.impl;

import com.wsf.infrastructure.security.extension.mobile.service.MobileUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class MobileUserDetailsServiceImpl implements MobileUserDetailsService {

    /**
     * 通过手机号加载用户信息
     *
     * @param mobile 手机号
     * @return {@link UserDetails}
     * @throws UsernameNotFoundException 异常
     */
    @Override
    public UserDetails loadUserByMobile(String mobile) throws UsernameNotFoundException {
        return null;
    }
}

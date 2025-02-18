package com.wsf.infrastructure.security.extension.mobile.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface MobileUserDetailsService {

    /**
     * 验证手机号和验证码
     * @param mobile    手机号
     * @param code      验证码
     * @return          {@link boolean}
     */
    boolean verifyCode(String mobile, String code);

    /**
     * 通过手机号加载用户信息
     *
     * @param mobile 手机号
     * @return {@link UserDetails}
     * @throws UsernameNotFoundException 异常
     */
    UserDetails loadUserByMobile(String mobile) throws UsernameNotFoundException;
}

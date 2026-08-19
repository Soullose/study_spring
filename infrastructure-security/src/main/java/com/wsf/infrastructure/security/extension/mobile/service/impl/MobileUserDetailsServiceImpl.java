package com.wsf.infrastructure.security.extension.mobile.service.impl;

import com.wsf.infrastructure.security.extension.mobile.service.MobileUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MobileUserDetailsServiceImpl implements MobileUserDetailsService {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 验证码校验
     *
     * @param mobile 手机号
     * @param code   验证码
     * @return {@link boolean}
     */
    @Override
    public boolean verifyCode(String mobile, String code) {
        return false;
    }

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

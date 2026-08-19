package com.wsf.infrastructure.security.extension.mobile.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import com.wsf.infrastructure.security.extension.mobile.service.MobileUserDetailsService;
import com.wsf.infrastructure.security.extension.mobile.token.MobileAuthenticationToken;

/**
 * 手机号登录认证
 */
public class MobileAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {
	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	private final MobileUserDetailsService mobileUserDetailsService;

	public MobileAuthenticationProvider(MobileUserDetailsService mobileUserDetailsService) {
		this.mobileUserDetailsService = mobileUserDetailsService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(MobileAuthenticationToken.class, authentication,
				() -> messages.getMessage("MobileAuthenticationProvider.onlySupports",
						"Only MobileAuthenticationProvider is supported"));
		MobileAuthenticationToken authenticationToken = (MobileAuthenticationToken) authentication;
		/// 手机号
		String mobilePhone = authenticationToken.getName();
		String code = (String) authenticationToken.getCredentials();
		try {

			UserDetails userDetails = mobileUserDetailsService.loadUserByMobile(mobilePhone);
			if (userDetails == null) {
				throw new UsernameNotFoundException("无法获取用户信息");
			}

			if (mobileUserDetailsService.verifyCode(mobilePhone, code)) {

			}
		} catch (Exception e) {
			log.error("手机号登录认证失败", e);
		}
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messages = new MessageSourceAccessor(messageSource);
	}
}

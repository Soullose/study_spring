package com.wsf.infrastructure.security.event;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.infrastructure.utils.RedisUtil;

import lombok.AllArgsConstructor;

/**
 * 认证事件处理
 *
 */
@Component
@AllArgsConstructor
public class AuthenticationEvents {
	private static final Logger log = LoggerFactory.getLogger(AuthenticationEvents.class);



	@EventListener
	public void onSuccess(AuthenticationSuccessEvent event) {
		/// 用户信息
		UserAccountDetail user = (UserAccountDetail) event.getAuthentication().getPrincipal();
		log.debug("授权成功:{}", user);
	}

	@EventListener
	public void onFailure(AbstractAuthenticationFailureEvent event) {
//		LoginAttemptService loginAttemptService = new LoginAttemptService();
		RedisUtil redisUtil = new RedisUtil();
		AuthenticationException exception = event.getException();
		String message = exception.getMessage();
		/// 用户名
		String username = (String) event.getAuthentication().getPrincipal();
		redisUtil.setStr("xxxx2", "222222222222222222222", 60000);
//		RAtomicLong rAtomicLong = redisUtil.rAtomicLong("attemptsKey");
//		long l = rAtomicLong.get();
//		if(l == 0){
//			rAtomicLong.expire(Duration.ofHours(1));
//		}
		log.debug("授权失败:{}", username);
		if (message != null) {
			log.debug("错误信息:{}", message);
			if (message.equals("Bad credentials")) {
				log.debug("用户名或密码错误");
//				loginAttemptService.recordFailedLogin(username);
				long l = redisUtil.increment("attemptsKey",Duration.ofMinutes(5));
				log.debug("l:{}", l);
			}
		}
	}

	@EventListener
	public void onLocked(AuthenticationFailureLockedEvent event) {
		/// 用户名
		String username = (String) event.getAuthentication().getPrincipal();
		log.debug("用户被锁定:{}", username);
	}
}

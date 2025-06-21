package com.wsf.infrastructure.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsf.infrastructure.utils.RedisUtil;

public class LoginAttemptService {

	private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

	/**
	 * 登录失败重试次数上限
	 */
	private static final int FAILED_RETRY_TIMES = 5;

	private final RedisUtil redisUtil = new RedisUtil();
	public LoginAttemptService() {
		logger.info("LoginAttemptService init");
	}


}
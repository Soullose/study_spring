package com.wsf.infrastructure.security.service;

import java.time.Duration;

import org.redisson.api.RAtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;

import com.wsf.infrastructure.utils.RedisUtil;

@Component
public class LoginAttemptService {

	private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

	private static final String LOCK_KEY_PREFIX = "account-lock:";
	private static final String FAILED_ATTEMPTS_KEY_PREFIX = "failed-attempts:";
	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final long LOCK_TIME_DURATION = 15; // 15分钟

	public LoginAttemptService() {
		logger.info("LoginAttemptService init");
	}

	/**
	 * 锁定账户
	 * 
	 * @param username
	 *            用户名
	 */
	public void lockUser(String username) {
		RedisUtil redisUtil = new RedisUtil();
		String lockKey = LOCK_KEY_PREFIX + username;
		redisUtil.set(lockKey, true, Duration.ofMinutes(LOCK_TIME_DURATION).toMinutes());
	}

	/**
	 * 记录登录失败次数
	 * 
	 * @param username
	 *            用户名
	 */
	public void recordFailedLogin(String username) {
		RedisUtil redisUtil = new RedisUtil();
		String lockKey = LOCK_KEY_PREFIX + username;
		String attemptsKey = FAILED_ATTEMPTS_KEY_PREFIX + username;
		/// 获取失败次数
		Long attemptsCounter = redisUtil.increment(attemptsKey, Duration.ofMinutes(LOCK_TIME_DURATION));

		if (redisUtil.hasKey(attemptsKey)) {
			if (attemptsCounter >= MAX_FAILED_ATTEMPTS) {
				throw new LockedException("账户被锁定,请于15分后重新登录");
			}
		}
	}

	/**
	 * 检测账户是否登录错误被锁定
	 * 
	 * @param username
	 *            用户名
	 */
	public void hasAttemptsLocked(String username) {
		RedisUtil redisUtil = new RedisUtil();
		String attemptsKey = FAILED_ATTEMPTS_KEY_PREFIX + username;
		RAtomicLong attemptsCounter = redisUtil.rAtomicLong(attemptsKey);
		if (redisUtil.hasKey(attemptsKey)) {
			if (attemptsCounter.get() >= MAX_FAILED_ATTEMPTS) {
				logger.error("用户名: {} 登录失败次数已超过最大限制", username);
				throw new LockedException("账户被锁定,请于15分后重新登录");
			}
		}
	}

	/**
	 * 检测账户是否被锁定
	 * 
	 * @param username
	 *            用户名
	 */
	public void hasLocked(String username) {
		RedisUtil redisUtil = new RedisUtil();
		String lockKey = LOCK_KEY_PREFIX + username;
		if (redisUtil.hasKey(lockKey)) {
			logger.error("用户名: {} 被锁定", username);
			throw new LockedException("账户被锁定,请于15分后重新登录");
		}
	}
}
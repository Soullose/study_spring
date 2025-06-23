package com.wsf.infrastructure.security.service;

import org.redisson.api.RAtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;

import com.wsf.infrastructure.utils.RedisUtil;

public class LoginAttemptService {

	private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);
	private static final String LOCK_KEY_PREFIX = "account-lock:";
	private static final String FAILED_ATTEMPTS_KEY_PREFIX = "failed-attempts:";
	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // 15分钟，毫秒

//	private final RedisUtil redisUtil = new RedisUtil();
	public LoginAttemptService() {
		logger.info("LoginAttemptService init");
	}

	public void recordFailedLogin(String username) {
		RedisUtil redisUtil = new RedisUtil();
		String lockKey = LOCK_KEY_PREFIX + username;
		String attemptsKey = FAILED_ATTEMPTS_KEY_PREFIX + username;
		/// 获取失败次数
		RAtomicLong rAtomicLong = redisUtil.rAtomicLong(attemptsKey);

		if (redisUtil.hasKey(attemptsKey)) {
			long currentCounter = rAtomicLong.get();
			if (currentCounter >= MAX_FAILED_ATTEMPTS) {
				throw new LockedException("账户被锁定,请于15分后重新登录");
			}
			Long attemptsCounter = redisUtil.increment(attemptsKey);
		}
	}

	// private RAtomicLong getRedisCounter(String key) {
	// RAtomicLong atomicLong = redisUtil.rAtomicLong(key);
	// if (atomicLong.get() == 0) {
	// /// 设置过期时间，15分钟
	// atomicLong.expire(Duration.ofMinutes(LOCK_TIME_DURATION));
	// }
	// return atomicLong;
	// }
}
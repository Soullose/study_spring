package com.wsf.infrastructure.security.service;

import java.time.Duration;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisTokenStore {
  private final RedissonClient redissonClient;
  private static final String KEY = "login:refresh:";

  /// 存储刷新Token
  public void saveRefreshToken(String userId, String jti) {
    redissonClient.getBucket(KEY + userId).set(jti, Duration.ofDays(7));
  }

  /// 校验
  public boolean isValid(String userId, String jti) {
    Object v = redissonClient.getBucket(KEY + userId).get();
    return v != null && v.toString().equals(jti);
  }

  /// 移除
  public void revoke(String userId) {
    redissonClient.getBucket(KEY + userId).delete();
  }
}

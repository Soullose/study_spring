package com.wsf.infrastructure.security.service;

import com.wsf.infrastructure.persistence.entity.user.UserAccount;
import com.wsf.infrastructure.security.domain.TokenPair;
import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.infrastructure.security.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenIssueService {
  private final JwtService jwtService;
  private final TokenRepository tokenRepository;
  private final RedisTokenStore redisTokenStore; // 见第 13 节

  public TokenPair issue(UserAccount account) {
    UserAccountDetail ud = new UserAccountDetail(account);
    String accessToken = jwtService.generateAccessToken(ud);
    String refreshToken = jwtService.generateRefreshToken(ud); // 含 jti
    // 撤销历史 token（DB 审计维度）
    revokeAllUserAccountTokens(account);
    saveUserToken(account, accessToken);
    // refresh token 写 Redis（有效性以 Redis 为准）
    redisTokenStore.saveRefreshToken(account.getId(), jwtService.extractJti(refreshToken));
    return new TokenPair(accessToken, refreshToken);
  }

  private void revokeAllUserAccountTokens(UserAccount account) {
  }

  private void saveUserToken(UserAccount account, String accessToken) {
  }
}

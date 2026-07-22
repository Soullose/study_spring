package com.wsf.infrastructure.security.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wsf.infrastructure.persistence.entity.token.Token;
import com.wsf.infrastructure.persistence.entity.token.enums.TokenType;
import com.wsf.infrastructure.persistence.entity.user.UserAccount;
import com.wsf.infrastructure.security.domain.*;
import com.wsf.infrastructure.security.repository.TokenRepository;
import com.wsf.infrastructure.security.repository.UserAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final PasswordEncoder passwordEncoder;

  private final TokenRepository tokenRepository;

  private final UserAccountRepository userAccountRepository;

  private final JwtService jwtService;

  private final AuthenticationManager authenticationManager;

  private final UserAccountDetailService userAccountDetailService;

  private final RedisTokenStore redisTokenStore;

  private final TokenIssueService tokenIssueService;

  public RegisterResponse register(RegisterRequest request) {
    UserAccount userAccount = UserAccount.builder()
        .username(request.getUsername())
        .password(passwordEncoder.encode(request.getPassword()))
        .enabled(true)
        .accountNonExpired(false)
        .accountNonLocked(false)
        .build();

    userAccountRepository.save(userAccount);

    // 简化注册逻辑，暂时不创建User实体
    // String jwtToken = jwtService.generateAccessToken(new UserAccountDetail(account));
    // saveUserToken(account, jwtToken);
    return RegisterResponse.builder().token(request.getFirstname() + request.getLastname()).build();
  }

  public AuthenticateResponse authenticate(AuthenticateRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(),
            request.getPassword()
        )
    );
    UserAccountDetail userDetails = userAccountDetailService.loadUserDetailByUsername(request.getUsername());
    String jwtToken = jwtService.generateAccessToken(userDetails);
    revokeAllUserAccountTokens(userDetails.getUserAccount());
    saveUserToken(userDetails.getUserAccount(), jwtToken);
    return AuthenticateResponse.builder().token(jwtToken).build();
  }

  /// 撤销所有token
  private void revokeAllUserAccountTokens(UserAccount account) {
    Set<Token> tokens = tokenRepository.findByUserAccount(account)
        .orElseThrow(NullPointerException::new);
    if (tokens.isEmpty())
      return;
    tokens.forEach(token -> {
      token.setRevoked(true);
      token.setExpired(true);
    });
    tokenRepository.saveAll(tokens);
  }

  /// 保存token
  private void saveUserToken(UserAccount account, String jwtToken) {
    // log.info("{}",LocalDateTime.now(Clock.system(ZoneId.of("CTT",ZoneId.SHORT_IDS))));
    Token token = new Token();
    token.setUserAccount(account);
    token.setToken(jwtToken);
    token.setTokenType(TokenType.BEARER);
    token.setExpired(false);
    token.setRevoked(false);
    token.setCreateDateTime(LocalDateTime.now());
    tokenRepository.save(token);
  }

  // AuthenticationService 新增
  public TokenPair refresh(String refreshToken) {
    String username = jwtService.extractUsername(refreshToken); // 签名+过期由 jjwt 校验，过期抛 ExpiredJwtException
    String jti = jwtService.extractJti(refreshToken);
    UserAccountDetail ud = userAccountDetailService.loadUserDetailByUsername(username);
    String userId = ud.getUserAccount().getId();
    // 比对 Redis：不一致 = 已撤销/被踢/旧 token 重放
    if (!redisTokenStore.isValid(userId, jti)) {
      throw new IllegalArgumentException("refresh token 已失效，请重新登录");
    }
    // 轮换：签发新双 token（issue 内部会更新 Redis 的 jti，旧 refresh 即刻作废）
    return tokenIssueService.issue(ud.getUserAccount());
  }
}

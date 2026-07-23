package com.wsf.infrastructure.security.service;

import com.wsf.infrastructure.persistence.entity.user.UserAccountPO;
import com.wsf.infrastructure.security.domain.*;
import com.wsf.infrastructure.security.repository.TokenRepository;
import com.wsf.infrastructure.security.repository.UserAccountPORepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;

    private final TokenRepository tokenRepository;

    private final UserAccountPORepository userAccountRepository;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final UserAccountDetailService userAccountDetailService;

    private final RedisTokenStore redisTokenStore;

    private final TokenIssueService tokenIssueService;

    public RegisterResponse register(RegisterRequest request) {
        // 1. 唯一性预检
        if (userAccountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已被占用: " + request.getUsername());
        }

        UserAccountPO userAccount = UserAccountPO.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountNonExpired(false)
                .accountNonLocked(false)
                .build();

        userAccountRepository.save(userAccount);
        TokenPair pair = tokenIssueService.issue(userAccount);
        // 简化注册逻辑，暂时不创建User实体
        // String jwtToken = jwtService.generateAccessToken(new UserAccountDetail(account));
        // saveUserToken(account, jwtToken);
        RegisterResponse registerResponse = new RegisterResponse(pair.accessToken(), pair.refreshToken());
        return registerResponse;
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        UserAccountDetail userDetails = userAccountDetailService.loadUserDetailByUsername(request.getUsername());
        TokenPair tokenPair = tokenIssueService.issue(userDetails.getUserAccount());
        return AuthenticateResponse.builder().accessToken(tokenPair.accessToken()).refreshToken(tokenPair.refreshToken()).build();
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

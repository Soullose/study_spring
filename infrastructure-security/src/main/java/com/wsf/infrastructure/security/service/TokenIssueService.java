package com.wsf.infrastructure.security.service;

import com.wsf.infrastructure.persistence.entity.token.Token;
import com.wsf.infrastructure.persistence.entity.token.enums.TokenType;
import com.wsf.infrastructure.persistence.entity.user.UserAccountPO;
import com.wsf.infrastructure.security.domain.TokenPair;
import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.infrastructure.security.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TokenIssueService {
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final RedisTokenStore redisTokenStore; // 见第 13 节

    public TokenPair issue(UserAccountPO account) {
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

    /// 撤销所有token
    private void revokeAllUserAccountTokens(UserAccountPO account) {
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
    private void saveUserToken(UserAccountPO account, String accessToken) {
        // log.info("{}",LocalDateTime.now(Clock.system(ZoneId.of("CTT",ZoneId.SHORT_IDS))));
        Token token = new Token();
        token.setUserAccount(account);
        token.setToken(accessToken);
        token.setTokenType(TokenType.BEARER);
        token.setExpired(false);
        token.setRevoked(false);
        token.setCreateDateTime(LocalDateTime.now());
        tokenRepository.save(token);
    }
}

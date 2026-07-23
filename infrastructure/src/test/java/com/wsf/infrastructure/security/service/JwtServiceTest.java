package com.wsf.infrastructure.security.service;

import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.infrastructure.persistence.entity.user.UserAccountPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService 单元测试")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    @DisplayName("应生成JWT Token")
    void should_generateToken() {
        UserAccountPO account = UserAccountPO.builder()
                .username("admin")
                .password("password")
                .enabled(true)
                .build();
        UserAccountDetail userDetails = new UserAccountDetail(account);

        String token = jwtService.generateAccessToken(userDetails);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("应提取用户名")
    void should_extractUsername() {
        UserAccountPO account = UserAccountPO.builder()
                .username("testuser")
                .password("password")
                .enabled(true)
                .build();
        UserAccountDetail userDetails = new UserAccountDetail(account);

        String token = jwtService.generateAccessToken(userDetails);
        String extracted = jwtService.extractUsername(token);

        assertThat(extracted).isEqualTo("testuser");
    }

    @Test
    @DisplayName("应验证有效Token")
    void should_validateValidToken() {
        UserAccountPO account = UserAccountPO.builder()
                .username("validuser")
                .password("password")
                .enabled(true)
                .build();
        UserAccountDetail userDetails = new UserAccountDetail(account);

        String token = jwtService.generateAccessToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("应拒绝无效Token（用户名不匹配）")
    void should_rejectToken_when_usernameMismatch() {
        UserAccountPO accountA = UserAccountPO.builder()
                .username("userA")
                .password("password")
                .enabled(true)
                .build();
        UserAccountPO accountB = UserAccountPO.builder()
                .username("userB")
                .password("password")
                .enabled(true)
                .build();

        String token = jwtService.generateAccessToken(new UserAccountDetail(accountA));
        boolean isValid = jwtService.isTokenValid(token, new UserAccountDetail(accountB));

        assertThat(isValid).isFalse();
    }
}

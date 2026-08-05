package com.wsf.infrastructure.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wsf.infrastructure.security.domain.RegisterRequest;
import com.wsf.infrastructure.security.domain.RegisterResponse;
import com.wsf.infrastructure.security.domain.TokenPair;
import com.wsf.infrastructure.security.repository.TokenRepository;
import com.wsf.infrastructure.security.repository.UserAccountPORepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService 测试")
class AuthenticationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private UserAccountPORepository userAccountRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserAccountDetailService userAccountDetailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private TokenIssueService tokenIssueService;

    @Test
    @DisplayName("应注册用户")
    void should_register() {
        RegisterRequest req = RegisterRequest.builder()
                .firstname("张")
                .lastname("三")
                .username("zhangsan")
                .password("password123")
                .build();

        when(passwordEncoder.encode(any())).thenReturn("$argon2id$hash");
        when(userAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // 测试方法里
        when(tokenIssueService.issue(any()))
                .thenReturn(new TokenPair("access-token", "refresh-token"));

        RegisterResponse resp = authenticationService.register(req);
        assertThat(resp).isNotNull();
    }
}

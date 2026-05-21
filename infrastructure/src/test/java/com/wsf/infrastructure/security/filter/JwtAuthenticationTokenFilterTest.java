package com.wsf.infrastructure.security.filter;

import com.wsf.infrastructure.security.repository.TokenRepository;
import com.wsf.infrastructure.security.service.JwtService;
import com.wsf.infrastructure.security.service.OpenUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtAuthenticationTokenFilter 测试")
class JwtAuthenticationTokenFilterTest {

    private JwtService jwtService;
    private OpenUserDetailsService userDetailsService;
    private TokenRepository tokenRepository;
    private JwtAuthenticationTokenFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(OpenUserDetailsService.class);
        tokenRepository = mock(TokenRepository.class);
        filter = new JwtAuthenticationTokenFilter(jwtService, userDetailsService, tokenRepository);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("shouldNotFilter 登录路径返回true")
    void should_notFilter_loginPath() throws Exception {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    @DisplayName("shouldNotFilter 非登录路径返回false")
    void should_notFilter_nonLoginPath() throws Exception {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }
}

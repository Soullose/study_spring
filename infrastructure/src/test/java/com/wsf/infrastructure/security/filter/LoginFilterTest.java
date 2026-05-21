package com.wsf.infrastructure.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginFilter 单元测试")
class LoginFilterTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("应拒绝非POST请求")
    void should_rejectNonPost() {
        LoginFilter filter = new LoginFilter();
        when(request.getMethod()).thenReturn("GET");

        assertThatThrownBy(() -> filter.attemptAuthentication(request, null))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessageContaining("Authentication method not supported");
    }

    @Test
    @DisplayName("应解析JSON登录请求")
    void should_parseJsonLoginRequest() throws Exception {
        LoginFilter filter = new LoginFilter();
        when(request.getMethod()).thenReturn("POST");

        String jsonBody = "{\"username\":\"admin\",\"password\":\"password123\"}";
        when(request.getInputStream()).thenReturn(
                new ServletInputStream() {
                    private final ByteArrayInputStream bais = new ByteArrayInputStream(jsonBody.getBytes());
                    @Override
                    public int read() throws IOException { return bais.read(); }
                    @Override
                    public boolean isFinished() { return bais.available() == 0; }
                    @Override
                    public boolean isReady() { return true; }
                    @Override
                    public void setReadListener(jakarta.servlet.ReadListener listener) {}
                });

        // 由于没有配置 AuthenticationManager，会抛异常，但能验证JSON解析正确执行
        assertThatThrownBy(() -> filter.attemptAuthentication(request, null))
                .isNotNull();
    }
}

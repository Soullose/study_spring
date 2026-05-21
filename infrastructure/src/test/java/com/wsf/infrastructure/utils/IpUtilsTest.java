package com.wsf.infrastructure.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("IpUtils 工具类测试")
class IpUtilsTest {

    @Test
    @DisplayName("应返回 unknown when 请求为null")
    void should_returnUnknown_when_requestNull() {
        assertThat(IpUtils.getIpAddr(null)).isEqualTo("unknown");
    }

    @Test
    @DisplayName("应从 X-Forwarded-For 获取IP")
    void should_getIpFromXForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn("192.168.1.100");

        assertThat(IpUtils.getIpAddr(request)).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("应从 Proxy-Client-IP 获取IP")
    void should_getIpFromProxyClientIP() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.1");

        assertThat(IpUtils.getIpAddr(request)).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("应识别内网IP")
    void should_identifyInternalIp() {
        assertThat(IpUtils.internalIp("192.168.1.1")).isTrue();
        assertThat(IpUtils.internalIp("10.0.0.1")).isTrue();
        assertThat(IpUtils.internalIp("172.16.0.1")).isTrue();
        assertThat(IpUtils.internalIp("127.0.0.1")).isTrue();
    }

    @Test
    @DisplayName("应识别外网IP")
    void should_identifyExternalIp() {
        assertThat(IpUtils.internalIp("8.8.8.8")).isFalse();
        assertThat(IpUtils.internalIp("114.114.114.114")).isFalse();
    }

    @Test
    @DisplayName("应获取主机名和IP")
    void should_getHostInfo() {
        assertThat(IpUtils.getHostIp()).isNotNull();
        assertThat(IpUtils.getHostName()).isNotNull();
    }

    @Test
    @DisplayName("应处理多级反向代理IP")
    void should_getMultistageReverseProxyIp() {
        assertThat(IpUtils.getMultistageReverseProxyIp("1.2.3.4, 5.6.7.8"))
                .isEqualTo("1.2.3.4");
    }
}

package com.wsf.infrastructure.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil 工具类测试")
class JwtUtilTest {

    @Test
    @DisplayName("应生成UUID")
    void should_generateUUID() {
        String uuid = JwtUtil.getUUID();
        assertThat(uuid).isNotNull().isNotEmpty().hasSize(32);
    }

    @Test
    @DisplayName("应生成不同的UUID")
    void should_generate_differentUUIDs() {
        String uuid1 = JwtUtil.getUUID();
        String uuid2 = JwtUtil.getUUID();
        assertThat(uuid1).isNotEqualTo(uuid2);
    }
}

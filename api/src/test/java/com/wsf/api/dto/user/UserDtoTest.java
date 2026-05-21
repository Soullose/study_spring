package com.wsf.api.dto.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserDto 测试")
class UserDtoTest {

    @Test
    @DisplayName("应通过Builder构造DTO")
    void should_buildDto() {
        LocalDateTime now = LocalDateTime.now();
        UserDto dto = UserDto.builder()
                .id("U001")
                .firstName("张")
                .lastName("三")
                .fullName("张三")
                .email("zhangsan@example.com")
                .phoneNumber("13800138000")
                .createTime(now)
                .build();

        assertThat(dto.getId()).isEqualTo("U001");
        assertThat(dto.getFullName()).isEqualTo("张三");
        assertThat(dto.getEmail()).isEqualTo("zhangsan@example.com");
    }
}

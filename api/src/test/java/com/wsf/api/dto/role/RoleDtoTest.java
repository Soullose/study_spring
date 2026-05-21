package com.wsf.api.dto.role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RoleDto 测试")
class RoleDtoTest {

    @Test
    @DisplayName("应通过Builder构造DTO")
    void should_buildDto() {
        RoleDto dto = RoleDto.builder()
                .id("R001")
                .code("ADMIN")
                .name("管理员")
                .enabled(true)
                .menuIds(Set.of("M001"))
                .build();

        assertThat(dto.getId()).isEqualTo("R001");
        assertThat(dto.getCode()).isEqualTo("ADMIN");
        assertThat(dto.getEnabled()).isTrue();
    }
}

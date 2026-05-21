package com.wsf.api.dto.menu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MenuDto 测试")
class MenuDtoTest {

    @Test
    @DisplayName("应通过Builder构造DTO")
    void should_buildDto() {
        MenuDto dto = MenuDto.builder()
                .id("M001")
                .name("用户管理")
                .menuType("MENU")
                .path("/users")
                .visible(true)
                .enabled(true)
                .sortOrder(1)
                .build();

        assertThat(dto.getId()).isEqualTo("M001");
        assertThat(dto.getMenuType()).isEqualTo("MENU");
        assertThat(dto.getVisible()).isTrue();
    }
}

package com.wsf.domain.model.role.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RoleName 值对象测试")
class RoleNameTest {

    @Test
    @DisplayName("应创建RoleName对象")
    void should_createRoleName() {
        RoleName name = new RoleName("管理员");
        assertThat(name.value()).isEqualTo("管理员");
    }

    @Test
    @DisplayName("应抛出异常 when 名称为空")
    void should_throwException_when_empty() {
        assertThatThrownBy(() -> new RoleName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role name cannot be empty");
    }

    @Test
    @DisplayName("应抛出异常 when 名称超过50字符")
    void should_throwException_when_tooLong() {
        String longName = "a".repeat(51);
        assertThatThrownBy(() -> new RoleName(longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role name cannot exceed 50 characters");
    }

    @Test
    @DisplayName("of静态方法应创建RoleName")
    void should_createViaOf() {
        RoleName name = RoleName.of("普通用户");
        assertThat(name.value()).isEqualTo("普通用户");
    }
}

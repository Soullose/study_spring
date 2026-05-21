package com.wsf.domain.model.role.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RoleCode 值对象测试")
class RoleCodeTest {

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "ROLE_USER", "SYSTEM_MANAGER", "OP1"})
    @DisplayName("应接受有效角色编码")
    void should_accept_validRoleCodes(String validCode) {
        assertThatCode(() -> new RoleCode(validCode)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("应抛出异常 when 编码为空")
    void should_throwException_when_empty() {
        assertThatThrownBy(() -> new RoleCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role code cannot be empty");
    }

    @Test
    @DisplayName("应抛出异常 when 编码以数字开头")
    void should_throwException_when_startsWithNumber() {
        assertThatThrownBy(() -> new RoleCode("1ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role code format");
    }

    @Test
    @DisplayName("应抛出异常 when 编码含小写字母")
    void should_throwException_when_containsLowercase() {
        assertThatThrownBy(() -> new RoleCode("admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role code format");
    }

    @Test
    @DisplayName("of静态方法应创建RoleCode")
    void should_createViaOf() {
        RoleCode code = RoleCode.of("ADMIN");
        assertThat(code.value()).isEqualTo("ADMIN");
    }
}

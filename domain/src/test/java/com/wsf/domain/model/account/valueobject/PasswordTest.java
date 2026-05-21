package com.wsf.domain.model.account.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Password 值对象测试")
class PasswordTest {

    @Test
    @DisplayName("应创建原始密码")
    void should_createRawPassword() {
        Password pwd = new Password("password123", false);
        assertThat(pwd.value()).isEqualTo("password123");
        assertThat(pwd.isEncoded()).isFalse();
    }

    @Test
    @DisplayName("应创建已编码密码")
    void should_createEncodedPassword() {
        Password pwd = new Password("$argon2id$encodedhash", true);
        assertThat(pwd.value()).isEqualTo("$argon2id$encodedhash");
        assertThat(pwd.isEncoded()).isTrue();
    }

    @Test
    @DisplayName("应抛出异常 when 密码为空")
    void should_throwException_when_empty() {
        assertThatThrownBy(() -> new Password(null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password cannot be empty");
    }

    @Test
    @DisplayName("应抛出异常 when 原始密码小于6位")
    void should_throwException_when_tooShort() {
        assertThatThrownBy(() -> new Password("12345", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password length must be between");
    }

    @Test
    @DisplayName("应抛出异常 when 原始密码大于20位")
    void should_throwException_when_tooLong() {
        String longPwd = "a".repeat(21);
        assertThatThrownBy(() -> new Password(longPwd, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ofRaw静态方法应创建原始密码")
    void should_createViaOfRaw() {
        Password pwd = Password.ofRaw("secure123");
        assertThat(pwd.isEncoded()).isFalse();
        assertThat(pwd.getValue()).isEqualTo("secure123");
    }

    @Test
    @DisplayName("ofEncoded静态方法应创建已编码密码")
    void should_createViaOfEncoded() {
        Password pwd = Password.ofEncoded("$argon2id$v=19$...");
        assertThat(pwd.isEncoded()).isTrue();
        assertThat(pwd.getValue()).isEqualTo("$argon2id$v=19$...");
    }
}

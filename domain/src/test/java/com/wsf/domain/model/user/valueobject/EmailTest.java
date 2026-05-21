package com.wsf.domain.model.user.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Email 值对象单元测试。
 */
@DisplayName("Email 值对象测试")
class EmailTest {

    @Test
    @DisplayName("应创建Email对象 when 邮箱格式有效")
    void should_createEmail_when_emailFormatValid() {
        Email email = new Email("test@example.com");
        assertThat(email.value()).isEqualTo("test@example.com");
        assertThat(email.isEmpty()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@domain.com",
            "a@b.co",
            "test.user@company.org",
            "user+tag@example.com",
            "user123@test.co.uk"
    })
    @DisplayName("应接受多种有效邮箱格式")
    void should_accept_validEmailFormats(String validEmail) {
        assertThatCode(() -> new Email(validEmail)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "@no-local",
            "no-at-sign",
            "no@tld.",
            "@.com",
            " spaces@test.com"
    })
    @DisplayName("应抛出异常 when 邮箱格式无效")
    void should_throwException_when_emailFormatInvalid(String invalidEmail) {
        assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("应接受 null 值")
    void should_acceptNull() {
        Email email = new Email(null);
        assertThat(email.value()).isNull();
        assertThat(email.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("应接受空字符串")
    void should_acceptEmptyString() {
        Email email = new Email("");
        assertThat(email.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("of静态方法应创建Email")
    void should_createViaOf() {
        Email email = Email.of("hello@world.com");
        assertThat(email.value()).isEqualTo("hello@world.com");
    }
}

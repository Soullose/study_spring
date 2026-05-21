package com.wsf.domain.model.user.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PhoneNumber 值对象测试")
class PhoneNumberTest {

    @ParameterizedTest
    @ValueSource(strings = {"13800138000", "15912345678", "18888888888", "13000000000"})
    @DisplayName("应接受有效手机号")
    void should_accept_validPhoneNumbers(String validPhone) {
        assertThatCode(() -> new PhoneNumber(validPhone)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "123", "1380013800a", "1380013800", "23800138000"})
    @DisplayName("应抛出异常 when 手机号格式无效")
    void should_throwException_when_phoneFormatInvalid(String invalidPhone) {
        assertThatThrownBy(() -> new PhoneNumber(invalidPhone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid phone number format");
    }

    @Test
    @DisplayName("应接受null值")
    void should_acceptNull() {
        PhoneNumber pn = new PhoneNumber(null);
        assertThat(pn.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("of静态方法应创建PhoneNumber")
    void should_createViaOf() {
        PhoneNumber pn = PhoneNumber.of("13800138000");
        assertThat(pn.value()).isEqualTo("13800138000");
    }
}

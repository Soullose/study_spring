package com.wsf.domain.model.user.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IdCardNumber 值对象测试")
class IdCardNumberTest {

    @Test
    @DisplayName("应接受有效18位身份证号")
    void should_accept_validIdCard() {
        assertThatCode(() -> new IdCardNumber("110101199003071234"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "abcdefghijklmnopqr", "11010119901307123A"})
    @DisplayName("应抛出异常 when 身份证号格式无效")
    void should_throwException_when_idCardFormatInvalid(String invalid) {
        assertThatThrownBy(() -> new IdCardNumber(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ID card number format");
    }

    @Test
    @DisplayName("应正确提取出生日期")
    void should_extractBirthDate() {
        IdCardNumber card = new IdCardNumber("110101199003071234");
        assertThat(card.getBirthDate()).isEqualTo("1990-03-07");
    }

    @Test
    @DisplayName("应正确判断性别")
    void should_determineGender() {
        IdCardNumber male = new IdCardNumber("110101199003071234");
        assertThat(male.getGender()).isEqualTo(1);

        IdCardNumber female = new IdCardNumber("110101199003072345");
        assertThat(female.getGender()).isEqualTo(2);
    }

    @Test
    @DisplayName("应接受null值")
    void should_acceptNull() {
        IdCardNumber card = new IdCardNumber(null);
        assertThat(card.isEmpty()).isTrue();
        assertThat(card.getBirthDate()).isNull();
        assertThat(card.getGender()).isNull();
    }

    @Test
    @DisplayName("of静态方法应创建IdCardNumber")
    void should_createViaOf() {
        IdCardNumber card = IdCardNumber.of("110101199003071234");
        assertThat(card.value()).isEqualTo("110101199003071234");
    }
}

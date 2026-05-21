package com.wsf.domain.model.user.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserName 值对象测试")
class UserNameTest {

    @Test
    @DisplayName("应创建UserName对象")
    void should_createUserName() {
        UserName name = new UserName("张", "三");
        assertThat(name.firstName()).isEqualTo("张");
        assertThat(name.lastName()).isEqualTo("三");
    }

    @Test
    @DisplayName("应抛出异常 when 姓和名都为空")
    void should_throwException_when_bothEmpty() {
        assertThatThrownBy(() -> new UserName("", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User name cannot be empty");
    }

    @Test
    @DisplayName("应拼接全名")
    void should_getFullName() {
        UserName name = new UserName("张", "三");
        assertThat(name.getFullName()).isEqualTo("张三");
    }

    @Test
    @DisplayName("getDisplayName应返回姓+名首字母")
    void should_getDisplayName() {
        UserName name = new UserName("张", "三丰");
        assertThat(name.getDisplayName()).isEqualTo("张三");
    }

    @Test
    @DisplayName("of静态方法应创建UserName")
    void should_createViaOf() {
        UserName name = UserName.of("李", "四");
        assertThat(name.firstName()).isEqualTo("李");
    }

    @Test
    @DisplayName("ofFullName应从单字创建")
    void should_createViaOfFullName_singleChar() {
        UserName name = UserName.ofFullName("王");
        assertThat(name.firstName()).isEqualTo("王");
        assertThat(name.lastName()).isEmpty();
    }

    @Test
    @DisplayName("ofFullName应拆分为姓和名")
    void should_createViaOfFullName_multiChar() {
        UserName name = UserName.ofFullName("欧阳锋");
        assertThat(name.firstName()).isEqualTo("欧");
        assertThat(name.lastName()).isEqualTo("阳锋");
    }

    @Test
    @DisplayName("ofFullName空值应抛出异常")
    void should_throwException_when_ofFullNameEmpty() {
        assertThatThrownBy(() -> UserName.ofFullName(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

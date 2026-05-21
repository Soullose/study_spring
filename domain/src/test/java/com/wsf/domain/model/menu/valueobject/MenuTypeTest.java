package com.wsf.domain.model.menu.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MenuType 枚举测试")
class MenuTypeTest {

    @Test
    @DisplayName("应包含3种菜单类型")
    void should_contain_threeTypes() {
        assertThat(MenuType.values()).hasSize(3);
    }

    @Test
    @DisplayName("getDescription应返回中文描述")
    void should_getDescription() {
        assertThat(MenuType.DIR.getDescription()).isEqualTo("目录");
        assertThat(MenuType.MENU.getDescription()).isEqualTo("菜单");
        assertThat(MenuType.BUTTON.getDescription()).isEqualTo("按钮");
    }
}

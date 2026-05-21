package com.wsf.domain.model.datapermission.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataScope 值对象测试")
class DataScopeTest {

    @Test
    @DisplayName("应包含5种数据范围")
    void should_contain_fiveScopes() {
        assertThat(DataScope.values()).hasSize(5);
    }

    @Test
    @DisplayName("ALL应包含所有范围")
    void should_ALL_include_all() {
        assertThat(DataScope.ALL.includes(DataScope.DEPT_AND_BELOW)).isTrue();
        assertThat(DataScope.ALL.includes(DataScope.DEPT)).isTrue();
        assertThat(DataScope.ALL.includes(DataScope.SELF)).isTrue();
        assertThat(DataScope.ALL.includes(DataScope.CUSTOM)).isTrue();
    }

    @Test
    @DisplayName("SELF不应包含上级范围")
    void should_SELF_not_include_upper() {
        assertThat(DataScope.SELF.includes(DataScope.ALL)).isFalse();
        assertThat(DataScope.SELF.includes(DataScope.DEPT)).isFalse();
    }

    @Test
    @DisplayName("同级范围应包含自身")
    void should_include_self() {
        assertThat(DataScope.DEPT.includes(DataScope.DEPT)).isTrue();
    }

    @Test
    @DisplayName("getDescription应返回中文描述")
    void should_getDescription() {
        assertThat(DataScope.ALL.getDescription()).isEqualTo("全部数据");
        assertThat(DataScope.SELF.getDescription()).isEqualTo("仅本人");
    }

    @Test
    @DisplayName("getLevel应返回级别")
    void should_getLevel() {
        assertThat(DataScope.ALL.getLevel()).isEqualTo(1);
        assertThat(DataScope.SELF.getLevel()).isEqualTo(4);
    }
}

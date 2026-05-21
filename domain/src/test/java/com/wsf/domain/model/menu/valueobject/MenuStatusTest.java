package com.wsf.domain.model.menu.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MenuStatus 值对象测试")
class MenuStatusTest {

    @Test
    @DisplayName("normal()应创建可见且启用的状态")
    void should_createNormalStatus() {
        MenuStatus status = MenuStatus.normal();
        assertThat(status.visible()).isTrue();
        assertThat(status.enabled()).isTrue();
        assertThat(status.isAvailable()).isTrue();
        assertThat(status.isVisible()).isTrue();
    }

    @Test
    @DisplayName("hidden()应创建隐藏但启用的状态")
    void should_createHiddenStatus() {
        MenuStatus status = MenuStatus.hidden();
        assertThat(status.visible()).isFalse();
        assertThat(status.enabled()).isTrue();
        assertThat(status.isAvailable()).isTrue();
        assertThat(status.isVisible()).isFalse();
    }

    @Test
    @DisplayName("disabled()应创建可见但禁用的状态")
    void should_createDisabledStatus() {
        MenuStatus status = MenuStatus.disabled();
        assertThat(status.visible()).isTrue();
        assertThat(status.enabled()).isFalse();
        assertThat(status.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("hiddenAndDisabled()应创建隐藏且禁用的状态")
    void should_createHiddenAndDisabledStatus() {
        MenuStatus status = MenuStatus.hiddenAndDisabled();
        assertThat(status.visible()).isFalse();
        assertThat(status.enabled()).isFalse();
        assertThat(status.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("show()应设置为可见")
    void should_show() {
        MenuStatus hidden = MenuStatus.hidden();
        MenuStatus shown = hidden.show();
        assertThat(shown.visible()).isTrue();
    }

    @Test
    @DisplayName("hide()应设置为隐藏")
    void should_hide() {
        MenuStatus normal = MenuStatus.normal();
        MenuStatus hidden = normal.hide();
        assertThat(hidden.visible()).isFalse();
    }

    @Test
    @DisplayName("enable()应启用菜单")
    void should_enable() {
        MenuStatus disabled = MenuStatus.disabled();
        MenuStatus enabled = disabled.enable();
        assertThat(enabled.enabled()).isTrue();
    }

    @Test
    @DisplayName("disable()应禁用菜单")
    void should_disable() {
        MenuStatus normal = MenuStatus.normal();
        MenuStatus disabled = normal.disable();
        assertThat(disabled.enabled()).isFalse();
    }
}

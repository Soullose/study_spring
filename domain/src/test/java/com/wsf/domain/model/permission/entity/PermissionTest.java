package com.wsf.domain.model.permission.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Permission 实体测试")
class PermissionTest {

    @Test
    @DisplayName("create()应创建权限")
    void should_createPermission() {
        Permission p = Permission.create("P001", "sys:user:create", "新增用户",
                "user", "create", "M001", "用户新增按钮权限");

        assertThat(p.getId()).isEqualTo("P001");
        assertThat(p.getCode()).isEqualTo("sys:user:create");
        assertThat(p.getResource()).isEqualTo("user");
        assertThat(p.getAction()).isEqualTo("create");
        assertThat(p.getMenuId()).isEqualTo("M001");
        assertThat(p.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("create()应抛出异常 when 编码为空")
    void should_throwException_when_codeEmpty() {
        assertThatThrownBy(() -> Permission.create("P002", null, "name",
                "res", "action", null, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Permission code cannot be empty");
    }

    @Test
    @DisplayName("linkMenu()应关联菜单")
    void should_linkMenu() {
        Permission p = Permission.create("P003", "sys:user:delete", "删除",
                "user", "delete", null, "");
        p.linkMenu("M002");
        assertThat(p.getMenuId()).isEqualTo("M002");
    }

    @Test
    @DisplayName("update()应更新权限信息")
    void should_update() {
        Permission p = Permission.create("P004", "sys:role:old", "旧名称",
                "old", "old", null, "旧描述");
        p.update("新名称", "role", "create", "新描述");

        assertThat(p.getName()).isEqualTo("新名称");
        assertThat(p.getResource()).isEqualTo("role");
        assertThat(p.getAction()).isEqualTo("create");
    }

    @Test
    @DisplayName("enable()/disable()应切换状态")
    void should_toggleEnabled() {
        Permission p = Permission.create("P005", "sys:test:toggle", "切换",
                "test", "toggle", null, "");

        p.disable();
        assertThat(p.isEnabled()).isFalse();

        p.enable();
        assertThat(p.isEnabled()).isTrue();
    }
}

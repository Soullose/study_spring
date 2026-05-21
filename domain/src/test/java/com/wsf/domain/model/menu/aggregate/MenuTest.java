package com.wsf.domain.model.menu.aggregate;

import com.wsf.domain.model.menu.valueobject.MenuStatus;
import com.wsf.domain.model.menu.valueobject.MenuType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Menu 聚合根测试")
class MenuTest {

    @Test
    @DisplayName("createDirectory()应创建目录类型菜单")
    void should_createDirectory() {
        Menu dir = Menu.createDirectory("M001", "系统管理", null, "setting", 1);
        assertThat(dir.getMenuType()).isEqualTo(MenuType.DIR);
        assertThat(dir.isDirectory()).isTrue();
        assertThat(dir.isMenu()).isFalse();
        assertThat(dir.isButton()).isFalse();
    }

    @Test
    @DisplayName("createMenu()应创建页面类型菜单")
    void should_createMenu() {
        Menu menu = Menu.createMenu("M002", "用户管理", "M001", "/users", "Users",
                "sys:user:list", "user", 1);
        assertThat(menu.getMenuType()).isEqualTo(MenuType.MENU);
        assertThat(menu.isMenu()).isTrue();
        assertThat(menu.getPath()).isEqualTo("/users");
        assertThat(menu.getPermission()).isEqualTo("sys:user:list");
    }

    @Test
    @DisplayName("createButton()应创建按钮类型菜单")
    void should_createButton() {
        Menu btn = Menu.createButton("M003", "新增用户", "M002", "sys:user:create", 1);
        assertThat(btn.getMenuType()).isEqualTo(MenuType.BUTTON);
        assertThat(btn.isButton()).isTrue();
        assertThat(btn.getPermission()).isEqualTo("sys:user:create");
    }

    @Test
    @DisplayName("addChild()应添加子菜单")
    void should_addChild() {
        Menu parent = Menu.createDirectory("M010", "根目录", null, "root", 0);
        Menu child = Menu.createMenu("M011", "子页面", "M010", "/child", "Child", "child:view", "child", 1);

        parent.addChild(child);
        assertThat(parent.getChildren()).hasSize(1);
    }

    @Test
    @DisplayName("removeChild()应移除子菜单")
    void should_removeChild() {
        Menu parent = Menu.createDirectory("M020", "父菜单", null, "parent", 0);
        Menu child = Menu.createMenu("M021", "子菜单", "M020", "/child", "Child", "child:view", "child", 1);
        parent.addChild(child);

        parent.removeChild("M021");
        assertThat(parent.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("isRoot()应判断是否为根菜单")
    void should_isRoot() {
        Menu root = Menu.createDirectory("M030", "根", null, "root", 0);
        Menu child = Menu.createMenu("M031", "子", "M030", "/sub", "Sub", "sub:view", "sub", 1);

        assertThat(root.isRoot()).isTrue();
        assertThat(child.isRoot()).isFalse();
    }

    @Test
    @DisplayName("show()/hide()应切换可见性")
    void should_toggleVisibility() {
        Menu menu = Menu.createMenu("M040", "页面", null, "/page", "Page", "page:view", "page", 1);
        menu.hide();
        assertThat(menu.isVisible()).isFalse();

        menu.show();
        assertThat(menu.isVisible()).isTrue();
    }

    @Test
    @DisplayName("enable()/disable()应切换启用状态")
    void should_toggleEnabled() {
        Menu menu = Menu.createMenu("M050", "页面", null, "/page", "Page", "page:view", "page", 1);
        menu.disable();
        assertThat(menu.isAvailable()).isFalse();

        menu.enable();
        assertThat(menu.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("update()应更新菜单信息")
    void should_update() {
        Menu menu = Menu.createMenu("M060", "旧名称", null, "/old", "Old", "old:view", "old", 1);
        menu.update("新名称", "/new", "New", "new:view", "new", 2);

        assertThat(menu.getName()).isEqualTo("新名称");
        assertThat(menu.getPath()).isEqualTo("/new");
        assertThat(menu.getPermission()).isEqualTo("new:view");
        assertThat(menu.getSortOrder()).isEqualTo(2);
    }
}

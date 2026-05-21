package com.wsf.domain.model.role.aggregate;

import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.role.valueobject.RoleCode;
import com.wsf.domain.model.role.valueobject.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Role 聚合根测试")
class RoleTest {

    @Test
    @DisplayName("create()应创建启用的角色")
    void should_createRole() {
        Role role = Role.create("R001", new RoleCode("ADMIN"),
                new RoleName("管理员"), "系统管理员");

        assertThat(role.getId()).isEqualTo("R001");
        assertThat(role.getCode().value()).isEqualTo("ADMIN");
        assertThat(role.getName().value()).isEqualTo("管理员");
        assertThat(role.isEnabled()).isTrue();
        assertThat(role.getMenus()).isEmpty();
        assertThat(role.getDataPermissions()).isEmpty();
    }

    @Test
    @DisplayName("update()应更新名称和描述")
    void should_updateRole() {
        Role role = Role.create("R002", new RoleCode("USER"),
                new RoleName("用户"), "普通用户");
        role.update(new RoleName("普通用户"), "更新后的描述");

        assertThat(role.getName().value()).isEqualTo("普通用户");
        assertThat(role.getDescription()).isEqualTo("更新后的描述");
    }

    @Test
    @DisplayName("enable()/disable()应切换启用状态")
    void should_toggleEnabled() {
        Role role = Role.create("R003", new RoleCode("GUEST"), new RoleName("访客"), "");

        role.disable();
        assertThat(role.isEnabled()).isFalse();

        role.enable();
        assertThat(role.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("assignMenu()应分配菜单")
    void should_assignMenu() {
        Role role = Role.create("R004", new RoleCode("MANAGER"), new RoleName("经理"), "");
        Menu menu = Menu.createMenu("M001", "首页", null, "/home", "Home", "sys:home:view", "home", 1);

        role.assignMenu(menu);
        assertThat(role.getMenus()).hasSize(1);
        assertThat(role.getMenuIds()).contains("M001");
    }

    @Test
    @DisplayName("removeMenu()应移除菜单")
    void should_removeMenu() {
        Role role = Role.create("R005", new RoleCode("OP"), new RoleName("操作员"), "");
        Menu menu = Menu.createMenu("M002", "用户管理", null, "/users", "Users", "sys:user:list", "user", 1);
        role.assignMenu(menu);
        assertThat(role.getMenus()).hasSize(1);

        role.removeMenu("M002");
        assertThat(role.getMenus()).isEmpty();
    }

    @Test
    @DisplayName("assignMenus()应批量分配菜单")
    void should_assignMenus() {
        Role role = Role.create("R006", new RoleCode("SUPER"), new RoleName("超级管理员"), "");
        Menu m1 = Menu.createButton("M003", "新增", "M002", "sys:user:create", 1);
        Menu m2 = Menu.createButton("M004", "删除", "M002", "sys:user:delete", 2);

        role.assignMenus(Set.of(m1, m2));
        assertThat(role.getMenus()).hasSize(2);
    }

    @Test
    @DisplayName("clearMenus()应清空菜单")
    void should_clearMenus() {
        Role role = Role.create("R007", new RoleCode("TEMP"), new RoleName("临时"), "");
        role.assignMenu(Menu.createMenu("M005", "test", null, "/test", "Test", "test:view", "test", 1));
        role.clearMenus();
        assertThat(role.getMenus()).isEmpty();
    }

    @Test
    @DisplayName("assignDataPermission()应分配数据权限")
    void should_assignDataPermission() {
        Role role = Role.create("R008", new RoleCode("DEPT_ADMIN"), new RoleName("部门管理员"), "");
        DataPermission dp = DataPermission.create("DP001", "本部门数据",
                ResourceType.DEPT, DataScope.DEPT, "仅查看本部门");

        role.assignDataPermission(dp);
        assertThat(role.getDataPermissions()).hasSize(1);
        assertThat(role.getDataPermissionIds()).contains("DP001");
    }

    @Test
    @DisplayName("rebuild()应恢复持久化的角色")
    void should_rebuildRole() {
        Role role = Role.rebuild("R009", new RoleCode("OLD"), new RoleName("旧角色"),
                "desc", true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        assertThat(role.getId()).isEqualTo("R009");
        assertThat(role.isEnabled()).isTrue();
    }
}

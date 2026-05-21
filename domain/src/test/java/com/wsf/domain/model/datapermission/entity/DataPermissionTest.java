package com.wsf.domain.model.datapermission.entity;

import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataPermission 实体测试")
class DataPermissionTest {

    @Test
    @DisplayName("create()应创建启用的数据权限")
    void should_createDataPermission() {
        DataPermission dp = DataPermission.create("DP001", "全部数据",
                ResourceType.DEPT, DataScope.ALL, "查看所有部门");

        assertThat(dp.getId()).isEqualTo("DP001");
        assertThat(dp.getName()).isEqualTo("全部数据");
        assertThat(dp.getDataScope()).isEqualTo(DataScope.ALL);
        assertThat(dp.isEnabled()).isTrue();
        assertThat(dp.isAll()).isTrue();
    }

    @Test
    @DisplayName("createCustom()应创建自定义数据权限")
    void should_createCustomDataPermission() {
        DataPermission dp = DataPermission.createCustom("DP002", "自定义部门",
                ResourceType.DEPT, Set.of("D01", "D02", "D03"), "指定部门");

        assertThat(dp.getDataScope()).isEqualTo(DataScope.CUSTOM);
        assertThat(dp.isCustom()).isTrue();
        assertThat(dp.getResourceIdSet()).containsExactlyInAnyOrder("D01", "D02", "D03");
    }

    @Test
    @DisplayName("getResourceIdSet()应解析逗号分隔的资源ID")
    void should_parseResourceIds() {
        DataPermission dp = DataPermission.rebuild("DP003", "test",
                ResourceType.DEPT, DataScope.CUSTOM, "D01,D02,D03",
                "desc", true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());

        assertThat(dp.getResourceIdSet()).hasSize(3);
    }

    @Test
    @DisplayName("getResourceIdSet()空值应返回空集合")
    void should_returnEmptySet_when_null() {
        DataPermission dp = DataPermission.create("DP004", "test",
                ResourceType.DEPT, DataScope.ALL, "");
        assertThat(dp.getResourceIdSet()).isEmpty();
    }

    @Test
    @DisplayName("update()应更新名称和范围")
    void should_update() {
        DataPermission dp = DataPermission.create("DP005", "旧名称",
                ResourceType.DEPT, DataScope.ALL, "旧描述");
        dp.update("新名称", DataScope.DEPT, "新描述");

        assertThat(dp.getName()).isEqualTo("新名称");
        assertThat(dp.getDataScope()).isEqualTo(DataScope.DEPT);
        assertThat(dp.getDescription()).isEqualTo("新描述");
    }

    @Test
    @DisplayName("updateResourceIds()应更新自定义资源列表")
    void should_updateResourceIds() {
        DataPermission dp = DataPermission.create("DP006", "test",
                ResourceType.DEPT, DataScope.ALL, "");
        dp.updateResourceIds(Set.of("D10", "D20"));

        assertThat(dp.getDataScope()).isEqualTo(DataScope.CUSTOM);
        assertThat(dp.getResourceIdSet()).contains("D10", "D20");
    }

    @Test
    @DisplayName("enable()/disable()应切换状态")
    void should_toggleEnabled() {
        DataPermission dp = DataPermission.create("DP007", "test",
                ResourceType.DEPT, DataScope.ALL, "");

        dp.disable();
        assertThat(dp.isEnabled()).isFalse();

        dp.enable();
        assertThat(dp.isEnabled()).isTrue();
    }
}

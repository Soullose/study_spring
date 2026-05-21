package com.wsf.app.service.impl;

import com.wsf.api.dto.datapermission.CreateDataPermissionRequest;
import com.wsf.api.dto.datapermission.DataPermissionDto;
import com.wsf.api.dto.datapermission.UpdateDataPermissionRequest;
import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import com.wsf.domain.repository.DataPermissionRepository;
import com.wsf.domain.service.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataPermissionServiceImpl 单元测试")
class DataPermissionServiceImplTest {

    @Mock
    private DataPermissionRepository dataPermissionRepository;
    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private DataPermissionServiceImpl dataPermissionService;

    @BeforeEach
    void setUp() {
        lenient().when(idGenerator.generate()).thenReturn("DP-GEN-001");
    }

    @Test
    @DisplayName("应创建数据权限")
    void should_createDataPermission() {
        CreateDataPermissionRequest req = new CreateDataPermissionRequest();
        req.setName("全部数据");
        req.setResourceType("DEPT");
        req.setDataScope("ALL");
        when(dataPermissionRepository.save(any(DataPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        DataPermissionDto result = dataPermissionService.createPermission(req);
        assertThat(result.getName()).isEqualTo("全部数据");
        assertThat(result.getDataScope()).isEqualTo("ALL");
    }

    @Test
    @DisplayName("应创建自定义数据权限")
    void should_createCustomDataPermission() {
        CreateDataPermissionRequest req = new CreateDataPermissionRequest();
        req.setName("自定义");
        req.setResourceType("DEPT");
        req.setDataScope("CUSTOM");
        req.setResourceIds(Set.of("D01", "D02"));
        when(dataPermissionRepository.save(any(DataPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        DataPermissionDto result = dataPermissionService.createPermission(req);
        assertThat(result.getDataScope()).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("应启用数据权限")
    void should_enablePermission() {
        DataPermission dp = DataPermission.create("DP001", "test",
                ResourceType.DEPT, DataScope.ALL, "");
        dp.disable();
        when(dataPermissionRepository.findById("DP001")).thenReturn(Optional.of(dp));
        when(dataPermissionRepository.save(any(DataPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        DataPermissionDto result = dataPermissionService.enablePermission("DP001");
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("应禁用数据权限")
    void should_disablePermission() {
        DataPermission dp = DataPermission.create("DP001", "test",
                ResourceType.DEPT, DataScope.ALL, "");
        when(dataPermissionRepository.findById("DP001")).thenReturn(Optional.of(dp));
        when(dataPermissionRepository.save(any(DataPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        DataPermissionDto result = dataPermissionService.disablePermission("DP001");
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("应更新数据权限")
    void should_updatePermission() {
        DataPermission dp = DataPermission.create("DP001", "old",
                ResourceType.DEPT, DataScope.ALL, "");
        when(dataPermissionRepository.findById("DP001")).thenReturn(Optional.of(dp));
        when(dataPermissionRepository.save(any(DataPermission.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateDataPermissionRequest req = new UpdateDataPermissionRequest();
        req.setName("new");
        req.setDataScope("DEPT");

        DataPermissionDto result = dataPermissionService.updatePermission("DP001", req);
        assertThat(result.getName()).isEqualTo("new");
    }

    @Test
    @DisplayName("应删除数据权限")
    void should_deletePermission() {
        dataPermissionService.deletePermission("DP001");
        verify(dataPermissionRepository).deleteById("DP001");
    }

    @Test
    @DisplayName("应查找所有数据权限")
    void should_findAll() {
        DataPermission dp1 = DataPermission.create("DP001", "a", ResourceType.DEPT, DataScope.ALL, "");
        DataPermission dp2 = DataPermission.create("DP002", "b", ResourceType.DEPT, DataScope.SELF, "");
        when(dataPermissionRepository.findAll()).thenReturn(List.of(dp1, dp2));

        assertThat(dataPermissionService.findAll()).hasSize(2);
    }
}

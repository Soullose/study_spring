package com.wsf.app.service.impl;

import com.wsf.api.dto.role.CreateRoleRequest;
import com.wsf.api.dto.role.RoleDto;
import com.wsf.api.dto.role.UpdateRoleRequest;
import com.wsf.domain.model.role.aggregate.Role;
import com.wsf.domain.model.role.valueobject.RoleCode;
import com.wsf.domain.model.role.valueobject.RoleName;
import com.wsf.domain.repository.DataPermissionRepository;
import com.wsf.domain.repository.MenuRepository;
import com.wsf.domain.repository.RoleRepository;
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
@DisplayName("RoleServiceImpl 单元测试")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private DataPermissionRepository dataPermissionRepository;
    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        lenient().when(idGenerator.generate()).thenReturn("ROLE-GEN-001");
    }

    @Test
    @DisplayName("应创建角色")
    void should_createRole() {
        CreateRoleRequest req = new CreateRoleRequest();
        req.setCode("ADMIN");
        req.setName("管理员");
        req.setDescription("系统管理员");

        when(roleRepository.existsByCode(any(RoleCode.class))).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        RoleDto result = roleService.createRole(req);

        assertThat(result.getCode()).isEqualTo("ADMIN");
        assertThat(result.getName()).isEqualTo("管理员");
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("应抛出异常 when 角色编码已存在")
    void should_throwException_when_codeExists() {
        CreateRoleRequest req = new CreateRoleRequest();
        req.setCode("ADMIN");

        when(roleRepository.existsByCode(any(RoleCode.class))).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("角色编码已存在");
    }

    @Test
    @DisplayName("应启用角色")
    void should_enableRole() {
        Role role = Role.create("R001", new RoleCode("USER"), new RoleName("用户"), "");
        role.disable();
        when(roleRepository.findById("R001")).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        RoleDto result = roleService.enableRole("R001");
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("应禁用角色")
    void should_disableRole() {
        Role role = Role.create("R001", new RoleCode("USER"), new RoleName("用户"), "");
        when(roleRepository.findById("R001")).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        RoleDto result = roleService.disableRole("R001");
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("应更新角色")
    void should_updateRole() {
        Role role = Role.create("R001", new RoleCode("USER"), new RoleName("用户"), "");
        when(roleRepository.findById("R001")).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateRoleRequest req = new UpdateRoleRequest();
        req.setName("新名称");

        RoleDto result = roleService.updateRole("R001", req);
        assertThat(result.getName()).isEqualTo("新名称");
    }

    @Test
    @DisplayName("应删除角色")
    void should_deleteRole() {
        roleService.deleteRole("R001");
        verify(roleRepository).deleteById("R001");
    }


    @Test
    @DisplayName("应查找所有角色")
    void should_findAll() {
        Role r1 = Role.create("R001", new RoleCode("ADMIN"), new RoleName("管理员"), "");
        Role r2 = Role.create("R002", new RoleCode("USER"), new RoleName("用户"), "");
        when(roleRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RoleDto> results = roleService.findAll();
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("应查找启用的角色")
    void should_findAllEnabled() {
        when(roleRepository.findAllEnabled()).thenReturn(List.of());
        assertThat(roleService.findAllEnabled()).isEmpty();
    }
}

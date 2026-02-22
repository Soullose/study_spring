package com.wsf.app.service.impl;

import com.wsf.api.dto.role.*;
import com.wsf.api.service.RoleService;
import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.role.aggregate.Role;
import com.wsf.domain.model.role.valueobject.RoleCode;
import com.wsf.domain.model.role.valueobject.RoleName;
import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.repository.MenuRepository;
import com.wsf.domain.repository.RoleRepository;
import com.wsf.domain.repository.DataPermissionRepository;
import com.wsf.infrastructure.jpa.id.CustomIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final DataPermissionRepository dataPermissionRepository;

    @Override
    @Transactional
    public RoleDto createRole(CreateRoleRequest request) {
        RoleCode code = new RoleCode(request.getCode());
        
        // 检查角色编码唯一性
        if (roleRepository.existsByCode(code)) {
            throw new IllegalArgumentException("角色编码已存在: " + request.getCode());
        }
        
        String roleId = CustomIdGenerator.generateId();
        RoleName name = new RoleName(request.getName());
        
        Role role = Role.create(roleId, code, name, request.getDescription());
        
        Role savedRole = roleRepository.save(role);
        return toDto(savedRole);
    }

    @Override
    @Transactional
    public RoleDto updateRole(String roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));
        
        RoleName name = request.getName() != null ? new RoleName(request.getName()) : null;
        role.update(name, request.getDescription());
        
        Role savedRole = roleRepository.save(role);
        return toDto(savedRole);
    }

    @Override
    public Optional<RoleDto> findById(String roleId) {
        return roleRepository.findById(roleId)
                .map(this::toDto);
    }

    @Override
    public List<RoleDto> findAll() {
        return roleRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<RoleDto> findAllEnabled() {
        return roleRepository.findAllEnabled().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        roleRepository.deleteById(roleId);
    }

    @Override
    @Transactional
    public RoleDto enableRole(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));
        
        role.enable();
        Role savedRole = roleRepository.save(role);
        return toDto(savedRole);
    }

    @Override
    @Transactional
    public RoleDto disableRole(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));
        
        role.disable();
        Role savedRole = roleRepository.save(role);
        return toDto(savedRole);
    }

    @Override
    @Transactional
    public RoleDto assignMenus(String roleId, Set<String> menuIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));
        
        Set<Menu> menus = menuRepository.findByIds(menuIds);
        role.assignMenus(menus);
        
        Role savedRole = roleRepository.save(role);
        return toDto(savedRole);
    }

    @Override
    @Transactional
    public RoleDto assignDataPermissions(String roleId, Set<String> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));
        
        Set<DataPermission> permissions = dataPermissionRepository.findByIds(permissionIds);
        role.assignDataPermissions(permissions);
        
        Role savedRole = roleRepository.save(role);
        return toDto(savedRole);
    }
    
    /**
     * 转换为DTO
     */
    private RoleDto toDto(Role role) {
        return RoleDto.builder()
                .id(role.getId())
                .code(role.getCode() != null ? role.getCode().value() : null)
                .name(role.getName() != null ? role.getName().value() : null)
                .description(role.getDescription())
                .enabled(role.isEnabled())
                .menuIds(role.getMenus().stream()
                        .map(Menu::getId)
                        .collect(Collectors.toSet()))
                .dataPermissionIds(role.getDataPermissions().stream()
                        .map(DataPermission::getId)
                        .collect(Collectors.toSet()))
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .build();
    }
}

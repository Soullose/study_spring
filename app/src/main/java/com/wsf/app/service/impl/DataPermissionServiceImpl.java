package com.wsf.app.service.impl;

import com.wsf.api.dto.datapermission.*;
import com.wsf.api.service.DataPermissionService;
import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import com.wsf.domain.repository.DataPermissionRepository;
import com.wsf.infrastructure.jpa.id.CustomIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限服务实现
 */
@Service
@RequiredArgsConstructor
public class DataPermissionServiceImpl implements DataPermissionService {

    private final DataPermissionRepository dataPermissionRepository;

    @Override
    @Transactional
    public DataPermissionDto createPermission(CreateDataPermissionRequest request) {
        String permissionId = CustomIdGenerator.generateId();
        
        ResourceType resourceType = ResourceType.valueOf(request.getResourceType());
        DataScope dataScope = DataScope.valueOf(request.getDataScope());
        
        DataPermission permission;
        if (dataScope == DataScope.CUSTOM && request.getResourceIds() != null) {
            permission = DataPermission.createCustom(
                    permissionId,
                    request.getName(),
                    resourceType,
                    request.getResourceIds(),
                    request.getDescription()
            );
        } else {
            permission = DataPermission.create(
                    permissionId,
                    request.getName(),
                    resourceType,
                    dataScope,
                    request.getDescription()
            );
        }
        
        DataPermission savedPermission = dataPermissionRepository.save(permission);
        return toDto(savedPermission);
    }

    @Override
    @Transactional
    public DataPermissionDto updatePermission(String permissionId, UpdateDataPermissionRequest request) {
        DataPermission permission = dataPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("数据权限不存在: " + permissionId));
        
        DataScope dataScope = request.getDataScope() != null 
                ? DataScope.valueOf(request.getDataScope()) 
                : null;
        
        permission.update(request.getName(), dataScope, request.getDescription());
        
        if (request.getResourceIds() != null) {
            permission.updateResourceIds(request.getResourceIds());
        }
        
        DataPermission savedPermission = dataPermissionRepository.save(permission);
        return toDto(savedPermission);
    }

    @Override
    public Optional<DataPermissionDto> findById(String permissionId) {
        return dataPermissionRepository.findById(permissionId)
                .map(this::toDto);
    }

    @Override
    public List<DataPermissionDto> findAll() {
        return dataPermissionRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<DataPermissionDto> findAllEnabled() {
        return dataPermissionRepository.findAllEnabled().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deletePermission(String permissionId) {
        dataPermissionRepository.deleteById(permissionId);
    }

    @Override
    @Transactional
    public DataPermissionDto enablePermission(String permissionId) {
        DataPermission permission = dataPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("数据权限不存在: " + permissionId));
        
        permission.enable();
        DataPermission savedPermission = dataPermissionRepository.save(permission);
        return toDto(savedPermission);
    }

    @Override
    @Transactional
    public DataPermissionDto disablePermission(String permissionId) {
        DataPermission permission = dataPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("数据权限不存在: " + permissionId));
        
        permission.disable();
        DataPermission savedPermission = dataPermissionRepository.save(permission);
        return toDto(savedPermission);
    }
    
    /**
     * 解析资源ID字符串
     */
    private Set<String> parseResourceIds(String resourceIds) {
        if (resourceIds == null || resourceIds.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(resourceIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
    
    /**
     * 转换为DTO
     */
    private DataPermissionDto toDto(DataPermission permission) {
        return DataPermissionDto.builder()
                .id(permission.getId())
                .name(permission.getName())
                .resourceType(permission.getResourceType() != null ? permission.getResourceType().name() : null)
                .dataScope(permission.getDataScope() != null ? permission.getDataScope().name() : null)
                .resourceIds(permission.getResourceIdSet())
                .description(permission.getDescription())
                .enabled(permission.isEnabled())
                .createTime(permission.getCreateTime())
                .updateTime(permission.getUpdateTime())
                .build();
    }
}

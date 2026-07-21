package com.wsf.infrastructure.persistence.repository.impl;

import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import com.wsf.domain.repository.DataPermissionRepository;
import com.wsf.infrastructure.persistence.converter.DataPermissionConverter;
import com.wsf.infrastructure.persistence.repository.DataPermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DataPermissionRepositoryImpl implements DataPermissionRepository {

    private final DataPermissionJpaRepository jpaRepository;
    private final DataPermissionConverter converter;

    @Override
    public DataPermission save(DataPermission permission) {
        var po = converter.toPO(permission);
        var savedPo = jpaRepository.save(po);
        return converter.toDomain(savedPo);
    }

    @Override
    public List<DataPermission> saveAll(List<DataPermission> permissions) {
        var pos = permissions.stream()
                .map(converter::toPO)
                .toList();
        var savedPos = jpaRepository.saveAll(pos);
        return savedPos.stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Optional<DataPermission> findById(String id) {
        return jpaRepository.findById(id)
                .map(converter::toDomain);
    }

    @Override
    public List<DataPermission> findAll() {
        return jpaRepository.findAll().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<DataPermission> findByIds(List<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Set<DataPermission> findByIds(Set<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(converter::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public List<DataPermission> findByDataScope(DataScope dataScope) {
        return jpaRepository.findByDataScope(dataScope).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<DataPermission> findByResourceType(ResourceType resourceType) {
        return jpaRepository.findByResourceType(resourceType).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<DataPermission> findAllEnabled() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByPermissionName(name);
    }
}

package com.wsf.infrastructure.persistence.repository.impl;

import com.wsf.domain.model.permission.entity.Permission;
import com.wsf.domain.repository.PermissionRepository;
import com.wsf.infrastructure.persistence.converter.PermissionConverter;
import com.wsf.infrastructure.persistence.repository.PermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionJpaRepository jpaRepository;
    private final PermissionConverter converter;

    @Override
    public Permission save(Permission permission) {
        var po = converter.toPO(permission);
        var savedPo = jpaRepository.save(po);
        return converter.toDomain(savedPo);
    }

    @Override
    public List<Permission> saveAll(List<Permission> permissions) {
        var pos = permissions.stream()
                .map(converter::toPO)
                .toList();
        var savedPos = jpaRepository.saveAll(pos);
        return savedPos.stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Optional<Permission> findById(String id) {
        return jpaRepository.findById(id)
                .map(converter::toDomain);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return jpaRepository.findByPermissionCode(code)
                .map(converter::toDomain);
    }

    @Override
    public List<Permission> findAll() {
        return jpaRepository.findAll().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Permission> findByIds(List<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Set<Permission> findByIds(Set<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(converter::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Permission> findByMenuId(String menuId) {
        return jpaRepository.findByMenuId(menuId).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Permission> findByResource(String resource) {
        return jpaRepository.findByResource(resource).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Permission> findAllEnabled() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByPermissionCode(code);
    }
}

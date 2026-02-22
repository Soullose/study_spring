package com.wsf.infrastructure.persistence.repository.impl;

import com.wsf.domain.model.role.aggregate.Role;
import com.wsf.domain.model.role.valueobject.RoleCode;
import com.wsf.domain.repository.RoleRepository;
import com.wsf.infrastructure.persistence.converter.RoleConverter;
import com.wsf.infrastructure.persistence.repository.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色仓储实现
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository jpaRepository;
    private final RoleConverter converter;

    @Override
    public Role save(Role role) {
        var po = converter.toPO(role);
        var savedPo = jpaRepository.save(po);
        return converter.toDomain(savedPo);
    }

    @Override
    public List<Role> saveAll(List<Role> roles) {
        var pos = roles.stream()
                .map(converter::toPO)
                .toList();
        var savedPos = jpaRepository.saveAll(pos);
        return savedPos.stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Optional<Role> findById(String id) {
        return jpaRepository.findById(id)
                .map(converter::toDomain);
    }

    @Override
    public Optional<Role> findByCode(RoleCode code) {
        if (code == null) {
            return Optional.empty();
        }
        return jpaRepository.findByCode(code.value())
                .map(converter::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Role> findByIds(List<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Set<Role> findByIds(Set<String> ids) {
        return jpaRepository.findByIdIn(ids).stream()
                .map(converter::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Role> findAllEnabled() {
        return jpaRepository.findAll().stream()
                .filter(po -> po.getName() != null) // 简化过滤条件
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(RoleCode code) {
        if (code == null) {
            return false;
        }
        return jpaRepository.existsByCode(code.value());
    }

    @Override
    public Optional<Role> findByIdWithMenus(String id) {
        // 简化实现，后续可通过JOIN FETCH优化
        return findById(id);
    }

    @Override
    public Optional<Role> findByIdWithDataPermissions(String id) {
        // 简化实现，后续可通过JOIN FETCH优化
        return findById(id);
    }

    @Override
    public Optional<Role> findByIdWithAllRelations(String id) {
        // 简化实现，后续可通过JOIN FETCH优化
        return findById(id);
    }
}

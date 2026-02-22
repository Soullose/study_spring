package com.wsf.infrastructure.persistence.repository.impl;

import com.wsf.domain.model.account.aggregate.UserAccount;
import com.wsf.domain.repository.UserAccountRepository;
import com.wsf.infrastructure.persistence.converter.UserAccountConverter;
import com.wsf.infrastructure.persistence.repository.UserAccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户账户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final UserAccountJpaRepository jpaRepository;
    private final UserAccountConverter converter;

    @Override
    public UserAccount save(UserAccount account) {
        var po = converter.toPO(account);
        var savedPo = jpaRepository.save(po);
        return converter.toDomain(savedPo);
    }

    @Override
    public List<UserAccount> saveAll(List<UserAccount> accounts) {
        var pos = accounts.stream()
                .map(converter::toPO)
                .toList();
        var savedPos = jpaRepository.saveAll(pos);
        return savedPos.stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Optional<UserAccount> findById(String id) {
        return jpaRepository.findById(id)
                .map(converter::toDomain);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(converter::toDomain);
    }

    @Override
    public Optional<UserAccount> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId)
                .map(converter::toDomain);
    }

    @Override
    public List<UserAccount> findAll() {
        return jpaRepository.findAll().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<UserAccount> findByRoleId(String roleId) {
        return jpaRepository.findByRoleId(roleId).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<UserAccount> findByIds(List<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return jpaRepository.findByUserId(userId).isPresent();
    }

    @Override
    public Optional<UserAccount> findByIdWithRoles(String id) {
        // 简化实现，后续可通过JOIN FETCH优化
        return findById(id);
    }

    @Override
    public Optional<UserAccount> findByIdWithSupplementaryMenus(String id) {
        // 简化实现，后续可通过JOIN FETCH优化
        return findById(id);
    }

    @Override
    public Optional<UserAccount> findByIdWithSupplementaryDataPermissions(String id) {
        // 简化实现，后续可通过JOIN FETCH优化
        return findById(id);
    }

    @Override
    public Optional<UserAccount> findByIdWithAllRelations(String id) {
        // 简化实现，后续可通过JOIN FETCH优化
        return findById(id);
    }
}

package com.wsf.infrastructure.persistence.repository.impl;

import com.wsf.domain.model.user.aggregate.User;
import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.PhoneNumber;
import com.wsf.domain.repository.UserRepository;
import com.wsf.infrastructure.persistence.converter.UserConverter;
import com.wsf.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserConverter converter;

    @Override
    public User save(User user) {
        var po = converter.toPO(user);
        var savedPo = jpaRepository.save(po);
        return converter.toDomain(savedPo);
    }

    @Override
    public List<User> saveAll(List<User> users) {
        var pos = users.stream()
                .map(converter::toPO)
                .toList();
        var savedPos = jpaRepository.saveAll(pos);
        return savedPos.stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id)
                .map(converter::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        if (email == null) {
            return Optional.empty();
        }
        return jpaRepository.findByEmail(email.value())
                .map(converter::toDomain);
    }

    @Override
    public Optional<User> findByPhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            return Optional.empty();
        }
        return jpaRepository.findByPhoneNumber(phoneNumber.value())
                .map(converter::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<User> findByIds(List<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(Email email) {
        if (email == null) {
            return false;
        }
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return jpaRepository.existsByPhoneNumber(phoneNumber.value());
    }
}

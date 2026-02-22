package com.wsf.app.service.impl;

import com.wsf.api.dto.user.*;
import com.wsf.api.service.UserService;
import com.wsf.domain.model.account.aggregate.UserAccount;
import com.wsf.domain.model.account.valueobject.Password;
import com.wsf.domain.model.user.aggregate.User;
import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.IdCardNumber;
import com.wsf.domain.model.user.valueobject.PhoneNumber;
import com.wsf.domain.model.user.valueobject.UserName;
import com.wsf.domain.repository.UserAccountRepository;
import com.wsf.domain.repository.UserRepository;
import com.wsf.infrastructure.jpa.id.CustomIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAccountRepository accountRepository;

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        // 验证邮箱唯一性
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(new Email(request.getEmail()))) {
                throw new IllegalArgumentException("邮箱已被使用: " + request.getEmail());
            }
        }
        
        // 验证手机号唯一性
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            if (userRepository.existsByPhoneNumber(new PhoneNumber(request.getPhoneNumber()))) {
                throw new IllegalArgumentException("手机号已被使用: " + request.getPhoneNumber());
            }
        }
        
        // 创建用户
        String userId = CustomIdGenerator.generateId();
        UserName name = new UserName(
            request.getFirstName() != null ? request.getFirstName() : "",
            request.getLastName() != null ? request.getLastName() : ""
        );
        Email email = request.getEmail() != null ? new Email(request.getEmail()) : null;
        PhoneNumber phoneNumber = request.getPhoneNumber() != null ? new PhoneNumber(request.getPhoneNumber()) : null;
        IdCardNumber idCardNumber = request.getIdCardNumber() != null ? new IdCardNumber(request.getIdCardNumber()) : null;
        
        User user = User.create(userId, name, email, phoneNumber, idCardNumber);
        
        // 如果需要同时创建账户
        if (Boolean.TRUE.equals(request.getCreateAccount())) {
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                throw new IllegalArgumentException("创建账户时用户名不能为空");
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new IllegalArgumentException("创建账户时密码不能为空");
            }
            
            String accountId = CustomIdGenerator.generateId();
            Password password = new Password(request.getPassword(), false);
            UserAccount account = UserAccount.create(accountId, request.getUsername(), password, userId);
            accountRepository.save(account);
        }
        
        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        
        UserName name = null;
        if (request.getFirstName() != null || request.getLastName() != null) {
            name = new UserName(
                request.getFirstName() != null ? request.getFirstName() : "",
                request.getLastName() != null ? request.getLastName() : ""
            );
        }
        
        Email email = request.getEmail() != null ? new Email(request.getEmail()) : null;
        PhoneNumber phoneNumber = request.getPhoneNumber() != null ? new PhoneNumber(request.getPhoneNumber()) : null;
        
        user.updateProfile(name, email, phoneNumber);
        
        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    @Override
    public Optional<UserDto> findById(String userId) {
        return userRepository.findById(userId)
                .map(this::toDto);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserDto createAccountForUser(String userId, String username, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        
        if (accountRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("用户已有账户");
        }
        
        if (accountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已被使用: " + username);
        }
        
        String accountId = CustomIdGenerator.generateId();
        Password pwd = new Password(password, false);
        UserAccount account = UserAccount.create(accountId, username, pwd, userId);
        accountRepository.save(account);
        
        return toDto(user);
    }

    @Override
    @Transactional
    public UserDto unlinkAccount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        
        accountRepository.findByUserId(userId).ifPresent(account -> {
            account.unlinkUser();
            accountRepository.save(account);
        });
        
        return toDto(user);
    }
    
    /**
     * 转换为DTO
     */
    private UserDto toDto(User user) {
        UserDto dto = UserDto.builder()
                .id(user.getId())
                .firstName(user.getName() != null ? user.getName().firstName() : null)
                .lastName(user.getName() != null ? user.getName().lastName() : null)
                .fullName(user.getName() != null ? user.getName().getFullName() : null)
                .realName(user.getRealName())
                .email(user.getEmail() != null ? user.getEmail().value() : null)
                .phoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber().value() : null)
                .idCardNumber(user.getIdCardNumber() != null ? user.getIdCardNumber().value() : null)
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
        
        // 查询关联账户
        accountRepository.findByUserId(user.getId()).ifPresent(account -> {
            dto.setHasAccount(true);
            dto.setAccountId(account.getId());
            dto.setUsername(account.getUsername());
        });
        
        if (dto.getHasAccount() == null) {
            dto.setHasAccount(false);
        }
        
        return dto;
    }
}

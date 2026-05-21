package com.wsf.app.service.impl;

import com.wsf.api.dto.user.CreateUserRequest;
import com.wsf.api.dto.user.UpdateUserRequest;
import com.wsf.api.dto.user.UserDto;
import com.wsf.domain.model.account.aggregate.UserAccount;
import com.wsf.domain.model.account.valueobject.Password;
import com.wsf.domain.model.user.aggregate.User;
import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.PhoneNumber;
import com.wsf.domain.model.user.valueobject.UserName;
import com.wsf.domain.repository.UserAccountRepository;
import com.wsf.domain.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserAccountRepository accountRepository;
    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        lenient().when(idGenerator.generate()).thenReturn("GEN-001", "GEN-002");
    }

    @Test
    @DisplayName("应创建用户")
    void should_createUser() {
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstName("张");
        req.setLastName("三");
        req.setEmail("zhangsan@example.com");
        req.setPhoneNumber("13800138000");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto result = userService.createUser(req);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("张");
        assertThat(result.getFullName()).isEqualTo("张三");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("应抛出异常 when 邮箱已存在")
    void should_throwException_when_emailExists() {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmail("existing@example.com");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("邮箱已被使用");
    }

    @Test
    @DisplayName("应抛出异常 when 手机号已存在")
    void should_throwException_when_phoneExists() {
        CreateUserRequest req = new CreateUserRequest();
        req.setPhoneNumber("13800138000");

        when(userRepository.existsByPhoneNumber(any(PhoneNumber.class))).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("手机号已被使用");
    }

    @Test
    @DisplayName("应通过ID查找用户")
    void should_findById() {
        User user = User.create("U001", new UserName("t", "u"), null, null, null);
        when(userRepository.findById("U001")).thenReturn(Optional.of(user));

        Optional<UserDto> result = userService.findById("U001");
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("U001");
    }

    @Test
    @DisplayName("应返回空 when ID不存在")
    void should_returnEmpty_when_idNotFound() {
        when(userRepository.findById("NONEXIST")).thenReturn(Optional.empty());
        assertThat(userService.findById("NONEXIST")).isEmpty();
    }

    @Test
    @DisplayName("应返回所有用户")
    void should_findAll() {
        User u1 = User.create("U001", new UserName("a", "1"), null, null, null);
        User u2 = User.create("U002", new UserName("b", "2"), null, null, null);
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserDto> results = userService.findAll();
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("应删除用户")
    void should_deleteUser() {
        userService.deleteUser("U001");
        verify(userRepository).deleteById("U001");
    }

    @Test
    @DisplayName("应更新用户")
    void should_updateUser() {
        User user = User.create("U001", new UserName("old", ""), null, null, null);
        when(userRepository.findById("U001")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("new");
        req.setLastName("name");

        UserDto result = userService.updateUser("U001", req);
        assertThat(result.getFullName()).isEqualTo("newname");
    }

    @Test
    @DisplayName("应抛出异常 when 更新不存在用户")
    void should_throwException_when_updateNonExistent() {
        when(userRepository.findById("NONEXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("NONEXIST", new UpdateUserRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户不存在");
    }
}

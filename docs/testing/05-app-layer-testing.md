# 05 — 应用层测试实战

> 预计阅读：25 分钟 | 难度：★★★☆☆ | 对应模块：`app/`

---

## 1. 应用层的特点

应用层（Application Layer）是业务用例的**编排者**：

```
控制器 → 应用服务 → 领域服务/仓储 → 数据库
```

应用层自己不包含业务逻辑（逻辑在领域层），它负责：
1. **接收请求**（从 Controller 来的 DTO）
2. **调用仓储**查询/持久化（调用的是接口，不是实现）
3. **调用领域对象**的方法
4. **返回结果**（DTO）

**测试特点**：
- ✅ 所有依赖都是接口 → 用 **Mock** 模拟
- ✅ 不需要 Spring 容器 → `@ExtendWith(MockitoExtension.class)`
- ⚠️ 需要 Mock 多个仓储 + 其他服务

---

## 2. 查看被测代码

以 `app/src/main/java/.../UserServiceImpl.java` 为例：

```java
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;         // 接口：需要 Mock
    private final UserAccountRepository accountRepository;
    private final IdGenerator idGenerator;

    public UserServiceImpl(UserRepository userRepository,
                           UserAccountRepository accountRepository,
                           IdGenerator idGenerator) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public UserDto createUser(CreateUserRequest req) {
        // ① 校验邮箱是否已存在
        if (userRepository.existsByEmail(new Email(req.getEmail()))) {
            throw new IllegalArgumentException("邮箱已被使用");
        }
        // ② 校验手机号是否已存在
        if (userRepository.existsByPhoneNumber(new PhoneNumber(req.getPhoneNumber()))) {
            throw new IllegalArgumentException("手机号已被使用");
        }
        // ③ 生成 ID，创建领域对象
        String id = idGenerator.generate();
        User user = User.create(id, new UserName(...), ...);
        // ④ 保存
        user = userRepository.save(user);
        // ⑤ 转换为 DTO 返回
        return UserDto.from(user);
    }
}
```

测试要覆盖什么？
- ✅ 正常创建 → `userRepository.save` 被调用
- ✅ 邮箱重复 → 抛异常
- ✅ 手机号重复 → 抛异常
- ✅ 按 ID 查找 → 找到 / 找不到
- ✅ 更新 → 找到用户 / 用户不存在
- ✅ 删除 → `userRepository.deleteById` 被调用

---

## 3. 完整范例：`app/src/test/java/.../UserServiceImplTest.java`

```java
package com.wsf.app.service.impl;

import com.wsf.api.dto.user.*;
import com.wsf.domain.model.account.aggregate.UserAccount;
import com.wsf.domain.model.user.aggregate.User;
import com.wsf.domain.model.user.valueobject.*;
import com.wsf.domain.repository.UserRepository;
import com.wsf.domain.repository.UserAccountRepository;
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

@ExtendWith(MockitoExtension.class)        // ← 必须
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

    // ==================== 替身对象 ====================

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAccountRepository accountRepository;

    @Mock
    private IdGenerator idGenerator;

    // ==================== 被测对象（替身注入） ====================

    @InjectMocks
    private UserServiceImpl userService;

    // ==================== 公共前置 ====================

    @BeforeEach
    void setUp() {
        // lenient：避免某些测试没用到这些 stub 时 Mockito 报警告
        lenient().when(idGenerator.generate()).thenReturn("GEN-001", "GEN-002");
    }

    // ==================== 创建用户 ====================

    @Test
    @DisplayName("应创建用户")
    void should_createUser() {
        // Given：准备请求
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstName("张");
        req.setLastName("三");
        req.setEmail("zhangsan@example.com");
        req.setPhoneNumber("13800138000");

        // When：模拟 save 返回传入的对象
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // Then：执行
        UserDto result = userService.createUser(req);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("张");
        assertThat(result.getFullName()).isEqualTo("张三");
        verify(userRepository).save(any(User.class));       // 验证 save 被调用
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

    // ==================== 查找用户 ====================

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

    // ==================== 删除用户 ====================

    @Test
    @DisplayName("应删除用户")
    void should_deleteUser() {
        userService.deleteUser("U001");

        verify(userRepository).deleteById("U001");          // 验证调用
    }

    // ==================== 更新用户 ====================

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
```

---

## 4. Given-When-Then 模式

上面的测试遵循经典的 **Given-When-Then** 结构：

```
// Given（准备数据 + 编排 Mock）
CreateUserRequest req = ...;
when(repo.findById("U001")).thenReturn(Optional.of(user));

// When（执行被测方法）
UserDto result = userService.createUser(req);

// Then（断言结果 + 验证 Mock 调用）
assertThat(result).isNotNull();
verify(repo).save(any());
```

---

## 5. 测试 `@BeforeEach` 中使用 `lenient()`

```java
@BeforeEach
void setUp() {
    lenient().when(idGenerator.generate()).thenReturn("GEN-001", "GEN-002");
}
```

**为什么用 `lenient()`？**

`@BeforeEach` 在每个测试方法之前都执行。如果你定义了 `idGenerator.generate()` 的行为，但某个测试方法（如"邮箱已存在时抛异常"）根本没调用 `idGenerator.generate()`，Mockito 就会报警告：`UnnecessaryStubbingException`。

`lenient()` 告诉 Mockito："这个 stub 不是每个测试都必须用，别报警告"。

---

## 6. `thenAnswer` — 当需要返回传入的参数时

```java
when(userRepository.save(any(User.class)))
    .thenAnswer(inv -> inv.getArgument(0));    // 返回第一个参数（即传入的 User）
```

这在测试中很常用：你 Mock 的 `save()` 不真的存数据库，但你需要拿回"被保存的对象"做断言。

```java
// 等价于普通写法
when(userRepository.save(any(User.class))).thenReturn(preCreatedUser);
// 但 preCreatedUser 可能还没创建 → 用 thenAnswer 动态返回
```

---

## 7. 编写应用层测试的步骤

### Step 1：看懂被测类

先搞清楚：
- 它依赖哪些接口？（→ 需要 `@Mock`）
- 每个方法的输入/输出是什么？
- 有哪些分支逻辑？

### Step 2：创建测试框架

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("XxxServiceImpl 单元测试")
class XxxServiceImplTest {

    @Mock private XxxRepository xxxRepository;
    @Mock private YyyService yyyService;

    @InjectMocks
    private XxxServiceImpl xxxService;

    // 测试方法...
}
```

### Step 3：为每个方法写测试矩阵

以 `createUser` 为例，列出所有情况：

| 场景 | Given（Mock 行为） | When | Then（预期） |
|------|---------------------|------|--------------|
| 正常创建 | 所有校验通过 | `createUser(req)` | 返回 DTO，`save` 被调用 |
| 邮箱已存在 | `existsByEmail` 返回 true | `createUser(req)` | 抛异常 |
| 手机号已存在 | `existsByPhoneNumber` 返回 true | `createUser(req)` | 抛异常 |
| 查找存在 | `findById` 返回 User | `findById("id")` | 返回 DTO |
| 查找不存在 | `findById` 返回 empty | `findById("id")` | 返回 empty |
| 删除 | — | `deleteUser("id")` | `deleteById` 被调用 |

每个场景一个 `@Test` 方法。

### Step 4：逐个实现

---

## 8. 常见问题

### Q：Mock 的接口在另一个模块怎么办？

依赖已在 `pom.xml` 中配置。`app/` 依赖了 `domain/` 和 `api/`，所以 `UserRepository`（在 `domain/` 模块）直接可见。

### Q：如果被测 Service 依赖了另一个 Service（不是接口）？

```java
// 用 @Mock 也可以 Mock 具体类（只要不是 final）
@Mock
private NotificationService notificationService;   // 具体类也能 Mock
```

### Q：被测类用 setter 注入而不是构造函数注入？

```java
@InjectMocks
private XxxService service;

// 如果 @InjectMocks 无法注入 setter，手动注入：
@BeforeEach
void setUp() {
    service = new XxxService();
    service.setRepository(mockRepo);       // 手动 setter 注入
}
```

---

## 9. 本模块已有测试

| 测试类 | 测试对象 |
|--------|----------|
| `UserServiceImplTest` | UserService 应用服务 |
| `RoleServiceImplTest` | RoleService 应用服务 |
| `MenuServiceImplTest` | MenuService 应用服务 |
| `DataPermissionServiceImplTest` | DataPermissionService 应用服务 |

---

## 10. 小结

| 要点 | 说明 |
|------|------|
| `@ExtendWith(MockitoExtension.class)` | 启用 Mockito |
| `@Mock` + `@InjectMocks` | 模拟依赖 + 自动注入 |
| `when(repo.xxx()).thenReturn(y)` | 编排 Mock 行为 |
| `verify(repo).xxx()` | 验证方法被调用 |
| `lenient().when(...)` | 避免不必要的 stub 警告 |
| Given-When-Then | 清晰的三段式结构 |

---

## 下一步

下一章进入最复杂的层—— **[06 - 基础设施层测试实战](06-infrastructure-testing.md)**，包括 Converter 测试、JPA Repository 测试、Security 组件测试。

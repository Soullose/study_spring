# 03 — Mockito 模拟指南

> 预计阅读：25 分钟 | 难度：★★★☆☆

---

## 1. 为什么需要 Mock？

现实中的代码都有依赖：

```java
// UserServiceImpl 依赖 UserRepository 和 IdGenerator
public class UserServiceImpl {
    private final UserRepository userRepository;    // 依赖 1：数据库访问
    private final IdGenerator idGenerator;          // 依赖 2：ID 生成器

    public UserDto createUser(CreateUserRequest req) {
        // 1. 从数据库检查邮箱是否存在
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("邮箱已被使用");
        }
        // 2. 生成 ID
        String id = idGenerator.generate();
        // 3. 保存用户
        userRepository.save(user);
        // ...
    }
}
```

**问题**：测试 `createUser()` 时，我们只关心它的**业务逻辑**（检查邮箱 → 生成ID → 保存），不想真的去连数据库或依赖 Redis。

**Mock 的作用**：用"替身"代替真实依赖，让我们能：
- 控制依赖的行为（让 `existsByEmail` 返回 `true` 或 `false`）
- 验证依赖是否被正确调用（`save` 被调了几次？传了什么参数？）
- 不连数据库也能跑测试（快！）

---

## 2. 核心概念：`@Mock` 和 `@InjectMocks`

```java
// 依赖：替身
@Mock
private UserRepository userRepository;

// 被测对象：把上面的替身注入进去
@InjectMocks
private UserServiceImpl userService;
```

看一个完整例子：

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// ① 必须加这个，告诉 JUnit 使用 Mockito
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock                               // ② 创建替身对象
    private UserRepository userRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks                        // ③ 创建被测对象，并把两个 @Mock 注入进去
    private UserServiceImpl userService;

    @Test
    void should_createUser() {
        // ④ 编排：告诉替身"当有人调用 generate() 时，返回 GEN-001"
        when(idGenerator.generate()).thenReturn("GEN-001");

        // ⑤ 编排：告诉替身"当有人调用 save() 时，把收到的参数原样返回"
        when(userRepository.save(any(User.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // ⑥ 执行：调用被测方法
        UserDto result = userService.createUser(request);

        // ⑦ 断言：检查结果
        assertThat(result).isNotNull();

        // ⑧ 验证：确认 save() 确实被调用了一次
        verify(userRepository).save(any(User.class));
    }
}
```

---

## 3. `when().thenReturn()` — 控制替身的行为

这是 Mockito 最常用的模式。基本公式：

```java
when(替身.某个方法(参数)).thenReturn(返回值);
```

### 3.1 返回固定值

```java
when(idGenerator.generate()).thenReturn("ID-001");

// 之后每次调用 generate() 都返回 "ID-001"
String id1 = idGenerator.generate();  // "ID-001"
String id2 = idGenerator.generate();  // "ID-001"
```

### 3.2 返回连续值（每次不同）

```java
when(idGenerator.generate())
    .thenReturn("ID-001")              // 第一次调用返回
    .thenReturn("ID-002")              // 第二次调用返回
    .thenReturn("ID-003");             // 第三次调用返回

String id1 = idGenerator.generate();  // "ID-001"
String id2 = idGenerator.generate();  // "ID-002"
String id3 = idGenerator.generate();  // "ID-003"
String id4 = idGenerator.generate();  // "ID-003"（最后一个重复）
```

### 3.3 抛异常

```java
when(userRepository.findById("BAD_ID"))
    .thenThrow(new RuntimeException("数据库挂了"));

assertThatThrownBy(() -> userRepository.findById("BAD_ID"))
    .isInstanceOf(RuntimeException.class);
```

### 3.4 根据参数动态返回

```java
when(userRepository.save(any(User.class)))
    .thenAnswer(invocation -> {
        User user = invocation.getArgument(0);   // 拿到第一个参数
        return user;                              // 把它原样返回
    });
```

### 3.5 `doReturn().when()` — 另一种写法

用于 `void` 方法或者需要避免实际调用的情况：

```java
// 标准写法（调用真实方法然后返回替身值）
when(mock.someMethod()).thenReturn("value");

// doReturn 写法（不调用真实方法，直接返回）
doReturn("value").when(mock).someMethod();
```

---

## 4. 参数匹配器

当你不关心参数的具体值，只关心类型时，用参数匹配器：

```java
import static org.mockito.ArgumentMatchers.*;

// any() — 匹配任意参数
when(repository.save(any(User.class))).thenReturn(savedUser);
when(repository.findById(anyString())).thenReturn(Optional.of(user));

// eq() — 精确匹配（配合其他匹配器时使用）
when(service.update(eq("ID001"), any(UpdateRequest.class))).thenReturn(result);

// isNull() / isNotNull()
when(repository.findByEmail(isNull())).thenThrow(new IllegalArgumentException());

// 自定义匹配器
when(repository.findByName(argThat(name -> name.startsWith("张"))))
    .thenReturn(List.of(userA, userB));
```

> ⚠️ **规则**：如果**任何一个**参数用了匹配器，**所有**参数都必须用匹配器。

```java
// ❌ 错误：混合了精确值和匹配器
when(service.update("ID001", any(UpdateRequest.class)));

// ✅ 正确：全用匹配器
when(service.update(eq("ID001"), any(UpdateRequest.class)));
```

---

## 5. `verify()` — 验证替身是否被正确调用

`when()` 是"在调用前编排"，`verify()` 是"在调用后检查"。

### 5.1 基本验证

```java
// 执行被测方法
userService.deleteUser("U001");

// 验证 deleteById 被调用了 1 次，参数是 "U001"
verify(userRepository).deleteById("U001");
```

### 5.2 验证调用次数

```java
verify(repository, times(1)).save(any());       // 恰好 1 次（默认）
verify(repository, times(3)).save(any());       // 恰好 3 次
verify(repository, atLeastOnce()).save(any());  // 至少 1 次
verify(repository, atLeast(2)).save(any());     // 至少 2 次
verify(repository, atMost(3)).save(any());      // 最多 3 次
verify(repository, never()).delete(any());      // 从未被调用
```

### 5.3 验证调用顺序

```java
InOrder inOrder = inOrder(repository, eventPublisher);

userService.createUser(request);

// 验证 save 先于 publishEvent 被调用
inOrder.verify(repository).save(any());
inOrder.verify(eventPublisher).publishEvent(any());
```

---

## 6. `@Spy` — 部分模拟

`@Mock` 完全是假的。`@Spy` 是对**真实对象**的包装，只有你指定的方法才被模拟：

```java
@Spy
private List<String> list = new ArrayList<>();   // 真实 ArrayList

@Test
void should_partiallyMock() {
    // 没有 stub 的方法走真实逻辑
    list.add("a");
    assertThat(list.size()).isEqualTo(1);        // 真实的 add 和 size

    // stub 特定方法
    doReturn(100).when(list).size();             // 只有 size() 被覆盖
    assertThat(list.size()).isEqualTo(100);      // 返回模拟值
}
```

> 📌 本项目中很少用 `@Spy`，了解即可。

---

## 7. `@Captor` — 捕获传给替身的参数

当你需要断言传给替身的参数内容时：

```java
@Captor
private ArgumentCaptor<User> userCaptor;

@Test
void should_saveWithCorrectData() {
    userService.createUser(request);

    // 捕获传给 save() 的 User 对象
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertThat(savedUser.getEmail().value()).isEqualTo("test@example.com");
    assertThat(savedUser.getFirstName()).isEqualTo("张");
}
```

---

## 8. `@Mock` 的替代：`mock()` 方法

如果不想用注解，也可以手写：

```java
@Test
void should_manualMock() {
    UserRepository mockRepo = mock(UserRepository.class);
    when(mockRepo.findById("U001")).thenReturn(Optional.of(user));

    UserServiceImpl service = new UserServiceImpl(mockRepo, idGenerator);
    // ...
}
```

但推荐用 `@Mock` + `@InjectMocks`，更清晰简洁。

---

## 9. 本项目 Mockito 实战范例

### 范例 1：`UserServiceImplTest` 的 setUp

```java
// app/src/test/java/.../UserServiceImplTest.java

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
        // lenient() 告诉 Mockito：这个 stub 不强制被调用，避免 UnnecessaryStubbing 警告
        lenient().when(idGenerator.generate()).thenReturn("GEN-001", "GEN-002");
    }
}
```

> `lenient()` 的作用：`@BeforeEach` 中设置的 stub 可能不是每个测试方法都用，加上 `lenient()` 避免 Mockito 报"不必要的 stub"警告。

### 范例 2：模拟仓储存在的情况

```java
@Test
@DisplayName("应通过ID查找用户")
void should_findById() {
    User user = User.create("U001", new UserName("t", "u"), null, null, null);
    when(userRepository.findById("U001")).thenReturn(Optional.of(user));

    Optional<UserDto> result = userService.findById("U001");

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo("U001");
}
```

### 范例 3：模拟仓储不存在的情况

```java
@Test
@DisplayName("应返回空 when ID不存在")
void should_returnEmpty_when_idNotFound() {
    when(userRepository.findById("NONEXIST")).thenReturn(Optional.empty());

    assertThat(userService.findById("NONEXIST")).isEmpty();
}
```

### 范例 4：验证删除操作

```java
@Test
@DisplayName("应删除用户")
void should_deleteUser() {
    userService.deleteUser("U001");

    // 验证：deleteById("U001") 被调用了恰好 1 次
    verify(userRepository).deleteById("U001");
}
```

---

## 10. Mockito 常见陷阱

### 陷阱 1：对 void 方法用 `when()`

```java
// ❌ 错误：void 方法不能用 when()
when(mock.someVoidMethod()).thenThrow(new RuntimeException());

// ✅ 正确：用 doThrow / doNothing
doThrow(new RuntimeException()).when(mock).someVoidMethod();
doNothing().when(mock).someVoidMethod();
```

### 陷阱 2：对 final 类或 static 方法 Mock

Mockito 默认**不能** mock final 类、static 方法、私有方法。

```java
// ❌ 不行（Mockito 3.x 默认不行）
@Mock
private StringUtils stringUtils;   // StringUtils 是 final 类

// 如果必须 mock final 类，需要配置（本项目不需要）
```

### 陷阱 3：`@InjectMocks` 注入失败

`@InjectMocks` 通过构造函数注入。确保被测类有对应的构造函数：

```java
// ✅ 有对应的构造函数 → 能注入
public class UserServiceImpl {
    public UserServiceImpl(UserRepository repo, IdGenerator gen) { ... }
}

// ❌ 只有 setter → @InjectMocks 注入不了
public class UserServiceImpl {
    public void setUserRepository(UserRepository repo) { ... }
}
```

### 陷阱 4：忘记 `@ExtendWith(MockitoExtension.class)`

```java
// ❌ 忘记加 → NPE，@Mock 没初始化
class MyTest {
    @Mock
    private Repository repo;
}

// ✅ 正确
@ExtendWith(MockitoExtension.class)
class MyTest {
    @Mock
    private Repository repo;
}
```

---

## 11. 小结

| 操作 | 语法 |
|------|------|
| 创建替身 | `@Mock` 字段 + `@ExtendWith(MockitoExtension.class)` |
| 注入替身 | `@InjectMocks` 被测对象 |
| 编排返回值 | `when(mock.method(args)).thenReturn(value)` |
| 编排抛异常 | `when(mock.method(args)).thenThrow(ex)` |
| 匹配任意参数 | `any(Type.class)` / `anyString()` / `eq(value)` |
| 验证调用 | `verify(mock).method(args)` |
| 验证次数 | `verify(mock, times(n)).method(args)` |
| 捕获参数 | `@Captor ArgumentCaptor<T>` |

---

## 下一步

基础工具全部就绪！接下来是实战篇，按项目分层逐个讲解：

👉 **[04 - 领域层测试实战](04-domain-layer-testing.md)**

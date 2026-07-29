# 02 — AssertJ 断言指南

> 预计阅读：20 分钟 | 难度：★★☆☆☆

---

## 1. 为什么用 AssertJ 而不是 JUnit 自带的断言？

JUnit 自带的 `assertEquals(expected, actual)` 够用但不好读。AssertJ 让你用**链式调用**写断言，像自然语言一样：

```java
// ❌ JUnit 自带：参数顺序容易搞反
assertEquals(5, result);

// ✅ AssertJ：从左到右读很自然
assertThat(result).isEqualTo(5);

// ❌ JUnit：多个断言要写多行
assertTrue(list.contains("a"));
assertEquals(3, list.size());

// ✅ AssertJ：链式调用一气呵成
assertThat(list)
    .contains("a")
    .hasSize(3);
```

---

## 2. 导入 AssertJ

```java
// 静态导入（推荐，代码更简洁）
import static org.assertj.core.api.Assertions.*;
```

> 本项目已在 `domain/pom.xml` 中引入 AssertJ 依赖，其他模块通过 `spring-boot-starter-test` 间接引入。

---

## 3. 核心模式：`assertThat(x).xxx()`

所有 AssertJ 断言都遵循这个模式：

```java
assertThat(被测对象).断言方法();
```

### 3.1 字符串断言

```java
String name = "张三";

assertThat(name)
    .isEqualTo("张三")               // 等于
    .isNotEqualTo("李四")            // 不等于
    .startsWith("张")                // 以...开头
    .endsWith("三")                  // 以...结尾
    .contains("三")                  // 包含
    .isNotEmpty()                    // 非空
    .hasSize(2);                     // 长度
```

### 3.2 数字断言

```java
int count = 42;

assertThat(count)
    .isEqualTo(42)
    .isPositive()                    // 正数
    .isNotNegative()                 // 非负数
    .isGreaterThan(40)               // 大于
    .isLessThan(100)                 // 小于
    .isBetween(1, 100);              // 在区间内
```

### 3.3 布尔断言

```java
boolean active = user.isActive();

assertThat(active).isTrue();
assertThat(active).isFalse();
```

### 3.4 空值断言

```java
Object obj = null;

assertThat(obj).isNull();
assertThat(obj).isNotNull();
```

### 3.5 集合和数组

```java
List<String> users = List.of("张三", "李四", "王五");

assertThat(users)
    .hasSize(3)                      // 大小
    .contains("张三")                // 包含某个元素
    .contains("张三", "李四")        // 包含多个（顺序无关）
    .containsExactly("张三", "李四", "王五")  // 包含且顺序完全一致
    .doesNotContain("赵六")          // 不包含
    .isNotEmpty()                    // 非空
    .first().isEqualTo("张三");      // 链到第一个元素继续断言

Map<String, Integer> map = Map.of("a", 1, "b", 2);

assertThat(map)
    .containsKey("a")                // 包含键
    .containsValue(1)                // 包含值
    .hasSize(2);
```

---

## 4. 异常断言

这是 AssertJ 最方便的地方之一，比 JUnit 的 `assertThrows` 更灵活：

```java
// 方式 1：断言会抛出指定类型异常
assertThatThrownBy(() -> new Email("invalid"))
    .isInstanceOf(IllegalArgumentException.class);     // 异常类型

// 方式 2：断言异常消息
assertThatThrownBy(() -> new Email("invalid"))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("Invalid email format");               // 精确匹配消息

// 方式 3：断言消息包含某关键字
assertThatThrownBy(() -> new Email("invalid"))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("Invalid email");            // 部分匹配

// 方式 4：断言不抛异常
assertThatCode(() -> new Email("test@example.com"))
    .doesNotThrowAnyException();

// 方式 5：对抛出的异常做复杂断言
assertThatThrownBy(() -> userService.findById(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("ID不能为空")
    .extracting("errorCode")                           // 提取属性
    .isEqualTo("ERR_001");
```

---

## 5. 提取和过滤

AssertJ 可以对集合中的对象进行**提取**和**过滤**后再断言：

```java
List<User> users = List.of(
    new User("张三", 25, true),
    new User("李四", 30, false),
    new User("王五", 28, true)
);

// 提取某个属性
assertThat(users)
    .extracting(User::getName)                 // 提取所有 name
    .containsExactly("张三", "李四", "王五");

// 提取多个属性
assertThat(users)
    .extracting(User::getName, User::getAge)
    .contains(
        tuple("张三", 25),                     // tuple() 匹配多个属性
        tuple("李四", 30)
    );

// 过滤后断言
assertThat(users)
    .filteredOn(user -> user.isActive())       // 只保留激活用户
    .hasSize(2)
    .extracting(User::getName)
    .contains("张三", "王五");
```

---

## 6. 自定义失败消息

当断言失败时，可以用 `as()` 添加描述性消息：

```java
User user = userRepository.findById("U001");

assertThat(user)
    .as("用户 U001 应该存在")                    // ← 失败时显示这个消息
    .isNotNull();

assertThat(user.getName())
    .as("检查用户 %s 的名字", user.getId())     // 支持 String.format 语法
    .isEqualTo("张三");
```

---

## 7. 软断言（Soft Assertions）

普通断言在第一个失败时就中断了。软断言会**执行所有断言，最后一起报告**：

```java
import org.assertj.core.api.SoftAssertions;

@Test
void should_validateAllFields() {
    User user = userService.findById("U001");

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(user.getId()).isEqualTo("U001");
    softly.assertThat(user.getName()).isEqualTo("张三");
    softly.assertThat(user.isActive()).isTrue();
    softly.assertThat(user.getRoles()).hasSize(2);
    softly.assertAll();                       // ← 在这里统一报告所有失败
}
```

> 💡 适用于需要一次性看到所有失败字段的场景，比如表单校验。

---

## 8. 本项目中的 AssertJ 实战范例

### 范例 1：`PasswordTest` — 值对象的边界条件

```java
// domain/src/test/java/.../PasswordTest.java（示意）

@Test
@DisplayName("应创建密码对象")
void should_createPassword() {
    Password pwd = new Password("MySecret123!");

    assertThat(pwd.value())                    // 取密码原文
        .isEqualTo("MySecret123!");
    assertThat(pwd.isEncrypted())              // 初始未加密
        .isFalse();
}
```

### 范例 2：`RoleTest` — 聚合根的行为测试

```java
// domain/src/test/java/.../RoleTest.java

@Test
@DisplayName("create()应创建启用的角色")
void should_createRole() {
    Role role = Role.create("R001",
        new RoleCode("ADMIN"),
        new RoleName("管理员"),
        "系统管理员");

    assertThat(role.getId()).isEqualTo("R001");
    assertThat(role.getCode().value()).isEqualTo("ADMIN");
    assertThat(role.isEnabled()).isTrue();      // 新角色默认启用
    assertThat(role.getMenus()).isEmpty();      // 初始无菜单
    assertThat(role.getDataPermissions()).isEmpty();
}
```

### 范例 3：`UserServiceImplTest` — 异常断言链

```java
// app/src/test/java/.../UserServiceImplTest.java

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
```

---

## 9. AssertJ vs JUnit 自带断言 对照表

| 需求 | JUnit 自带 | AssertJ |
|------|-----------|---------|
| 相等 | `assertEquals(expected, actual)` | `assertThat(actual).isEqualTo(expected)` |
| 真假 | `assertTrue(condition)` | `assertThat(condition).isTrue()` |
| null | `assertNull(x)` / `assertNotNull(x)` | `assertThat(x).isNull()` / `.isNotNull()` |
| 抛异常 | `assertThrows(Class, lambda)` | `assertThatThrownBy(lambda).isInstanceOf(Class)` |
| 集合大小 | `assertEquals(3, list.size())` | `assertThat(list).hasSize(3)` |
| 包含元素 | `assertTrue(list.contains(e))` | `assertThat(list).contains(e)` |
| 字符串包含 | `assertTrue(s.contains(sub))` | `assertThat(s).contains(sub)` |
| 提取属性 | 需要手写循环 | `assertThat(list).extracting(Getter)` |

**建议**：在本项目中统一使用 AssertJ，保持代码风格一致。

---

## 10. 小结

| 语法 | 说明 |
|------|------|
| `assertThat(x).isEqualTo(y)` | 基本的相等断言 |
| `assertThat(list).hasSize(n).contains(e)` | 集合链式断言 |
| `assertThatThrownBy(lambda).isInstanceOf(Ex.class)` | 异常断言 |
| `assertThatCode(lambda).doesNotThrowAnyException()` | 不抛异常 |
| `assertThat(list).extracting(Getter)` | 提取属性 |
| `.as("描述")` | 自定义失败消息 |
| `SoftAssertions` | 软断言，不中断 |

---

## 下一步

掌握了断言库后，下一章学习 **[03 - Mockito 模拟指南](03-mockito-guide.md)** — 如何模拟依赖对象，让你的测试真正"单元化"！

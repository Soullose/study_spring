# 01 — JUnit 5 零基础

> 预计阅读：30 分钟 | 难度：★☆☆☆☆

---

## 1. 什么是单元测试？

单元测试就是对代码中的**最小可测试单元**（通常是一个方法）进行正确性验证。

**举个例子**：你写了一个邮箱校验方法，测试就是给它各种输入（`test@example.com`、`invalid`、`null`），检查它是否返回了预期的结果。

### 为什么要写测试？

| 好处 | 说明 |
|------|------|
| 🛡️ **防止回归** | 改了 A 处的代码，测试会告诉你 B 处是否被搞坏了 |
| 📖 **即文档** | 测试本身就是最好的使用说明——看测试就知道方法怎么用 |
| 🧠 **辅助设计** | 不好测试的代码往往设计有问题，测试倒逼你写更好的代码 |
| ⚡ **快速反馈** | 不用启动整个应用，2 秒就知道改对了没 |

---

## 2. 你的第一个测试

### 2.1 被测代码

假设 `src/main/java/com/example/Calculator.java`：

```java
package com.example;

public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

### 2.2 测试代码

在 `src/test/java/com/example/CalculatorTest.java` 创建：

```java
package com.example;

import org.junit.jupiter.api.Test;                   // ① 导入 @Test 注解
import static org.junit.jupiter.api.Assertions.*;    // ② 导入断言方法

class CalculatorTest {                               // ③ 测试类（不需要 public）

    @Test                                            // ④ 标记这是一个测试方法
    void should_addTwoNumbers() {                    // ⑤ 方法名：should_做什么_when_什么条件
        Calculator calc = new Calculator();
        int result = calc.add(2, 3);                // ⑥ 执行被测方法
        assertEquals(5, result);                     // ⑦ 断言：期望值 5，实际值 result
    }
}
```

逐行解释：

- **① `@Test`**：告诉 JUnit "这是一个测试方法"。没有这个注解，JUnit 不会执行它。
- **② `assertEquals`**：断言方法。如果期望值和实际值不相等，测试**失败**。
- **③ 类不需要 `public`**：JUnit 5 中测试类和测试方法都可以是包级私有的。
- **④ 方法名**：用描述性的英文短语，能一眼看出测试意图。

### 2.3 运行测试

```bash
# Maven 命令行
mvn test -Dtest=CalculatorTest

# IDEA 中：点击方法左边的绿色三角 ▶ 直接运行
```

输出：
```
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

**绿色 = 通过 ✅，红色 = 失败 ❌**。

---

## 3. 核心注解

JUnit 5 通过**注解**来控制测试的执行流程。

### 3.1 `@Test` — 标记测试方法

```java
@Test
void myTestMethod() {
    // 测试逻辑
}
```

这是最基本、最常用的注解。

### 3.2 `@BeforeEach` / `@AfterEach` — 每个测试前后执行

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

class MyTest {

    private Calculator calc;

    @BeforeEach                    // 每个 @Test 方法执行前运行
    void setUp() {
        calc = new Calculator();   // 初始化被测对象
        System.out.println("准备完毕");
    }

    @AfterEach                     // 每个 @Test 方法执行后运行
    void tearDown() {
        calc = null;               // 清理资源
        System.out.println("清理完毕");
    }

    @Test
    void test1() { /* calc 是全新的 */ }

    @Test
    void test2() { /* calc 又是一个全新的 */ }
}
```

**关键点**：JUnit 为**每个** `@Test` 方法创建一个新的测试类实例。`@BeforeEach` 保证每次测试都从干净状态开始。

### 3.3 `@BeforeAll` / `@AfterAll` — 所有测试前后执行一次

```java
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

class MyTest {

    @BeforeAll                     // 所有测试前执行一次（必须是 static）
    static void initAll() {
        System.out.println("只执行一次：连接数据库");
    }

    @AfterAll                      // 所有测试后执行一次（必须是 static）
    static void cleanAll() {
        System.out.println("只执行一次：关闭数据库连接");
    }
}
```

⚠️ `@BeforeAll` / `@AfterAll` 方法**必须是 `static`**。

### 3.4 `@DisplayName` — 给测试起个中文名

```java
@Test
@DisplayName("应正确计算两数之和")
void should_addTwoNumbers() {
    assertEquals(5, new Calculator().add(2, 3));
}
```

IDEA 的测试运行窗口会显示 `@DisplayName` 的文字，比看驼峰方法名更友好。

---

## 4. 测试生命周期

一张图看清执行顺序：

```
@BeforeAll (static)  ← 只执行一次
    │
    ├─ 创建测试类实例 1
    │   @BeforeEach
    │   @Test testA()
    │   @AfterEach
    │
    ├─ 创建测试类实例 2 （全新实例！）
    │   @BeforeEach
    │   @Test testB()
    │   @AfterEach
    │
@AfterAll (static)   ← 只执行一次
```

**核心设计**：测试之间完全隔离，testA 的状态不会影响 testB。

---

## 5. 断言方法大全

断言 = 检查结果是否符合预期。JUnit 5 自带了这些断言方法：

```java
import static org.junit.jupiter.api.Assertions.*;

// 相等性
assertEquals(5, result);                    // 期望值 == 实际值
assertEquals(5, result, "结果应该是5");       // 失败时显示自定义消息

// 真假
assertTrue(list.isEmpty());                 // 断言为 true
assertFalse(user.isActive());               // 断言为 false

// 空值
assertNull(error);                          // 断言为 null
assertNotNull(user);                        // 断言不为 null

// 同一个引用
assertSame(expectedObj, actualObj);         // 断言是同一个对象（==）

// 异常
assertThrows(IllegalArgumentException.class, () -> {
    new Email("invalid");                   // 期望这行抛异常
});

// 不抛异常
assertDoesNotThrow(() -> {
    new Email("test@example.com");
});

// 超时
assertTimeout(Duration.ofSeconds(1), () -> {
    service.longRunningMethod();
});

// 数组
assertArrayEquals(new int[]{1,2,3}, resultArray);
```

> ⚠️ JUnit 自带的断言比较基础。本项目推荐使用 **AssertJ**（见第 02 章），它更强大、更易读。

---

## 6. `@ParameterizedTest` — 参数化测试

当你需要用**多组输入**测试同一个逻辑时，不要复制粘贴测试方法！用参数化测试：

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @ParameterizedTest                     // 注意：不是 @Test，是 @ParameterizedTest
    @ValueSource(strings = {               // 数据来源：一个字符串数组
        "test@example.com",
        "user@domain.com",
        "a@b.co"
    })
    @DisplayName("应接受有效邮箱格式")
    void should_acceptValidEmails(String validEmail) {   // 参数依次传入
        assertDoesNotThrow(() -> new Email(validEmail));
    }
}
```

JUnit 会自动运行 **3 次**这个测试，每次用一个不同的 email 值。

### 常用数据源

```java
// 字符串列表
@ValueSource(strings = {"a", "b", "c"})

// 整数列表
@ValueSource(ints = {1, 2, 3})

// 布尔列表
@ValueSource(booleans = {true, false})

// CSV 格式（多参数）
@CsvSource({
    "1, 2, 3",     // a=1, b=2, expected=3
    "0, 0, 0",
    "-1, 1, 0"
})
void should_add(int a, int b, int expected) {
    assertEquals(expected, calc.add(a, b));
}

// 空值和空字符串
@NullAndEmptySource
void should_handleNullAndEmpty(String input) {
    assertTrue(isValid(input));
}
```

---

## 7. 测试方法命名规范

本项目采用 **`should_做什么_when_什么条件`** 模式：

```java
// ✅ 好的命名（一眼看出意图）
void should_throwException_when_emailFormatInvalid() { ... }
void should_createUser() { ... }
void should_returnEmpty_when_idNotFound() { ... }
void should_validateValidToken() { ... }

// ❌ 不好的命名（不知道测什么）
void test1() { ... }
void testEmail() { ... }
void emailValidationTest() { ... }
```

**命名公式**：`should_` + 预期行为 + `_when_` + 触发条件（条件可选）

---

## 8. 实战：看懂本项目的 EmailTest

打开 `domain/src/test/java/.../EmailTest.java`，我们用刚学的知识来逐段分析：

```java
// ① 类级别的 @DisplayName，在测试报告中显示
@DisplayName("Email 值对象测试")
class EmailTest {

    // ② 普通测试：只测一组数据
    @Test
    @DisplayName("应创建Email对象 when 邮箱格式有效")
    void should_createEmail_when_emailFormatValid() {
        Email email = new Email("test@example.com");
        assertThat(email.value()).isEqualTo("test@example.com");   // ← 这是 AssertJ 写法
        assertThat(email.isEmpty()).isFalse();
    }

    // ③ 参数化测试：用 5 组合法邮箱测试同一逻辑
    @ParameterizedTest
    @ValueSource(strings = {
        "user@domain.com",
        "a@b.co",
        "test.user@company.org",
        "user+tag@example.com",
        "user123@test.co.uk"
    })
    @DisplayName("应接受多种有效邮箱格式")
    void should_accept_validEmailFormats(String validEmail) {
        assertThatCode(() -> new Email(validEmail)).doesNotThrowAnyException();
    }

    // ④ 参数化测试：测试非法邮箱时抛异常
    @ParameterizedTest
    @ValueSource(strings = {"invalid", "@no-local", "no-at-sign"})
    @DisplayName("应抛出异常 when 邮箱格式无效")
    void should_throwException_when_emailFormatInvalid(String invalidEmail) {
        assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    // ⑤ 边界条件测试：null 和空字符串
    @Test
    @DisplayName("应接受 null 值")
    void should_acceptNull() {
        Email email = new Email(null);
        assertThat(email.value()).isNull();
        assertThat(email.isEmpty()).isTrue();
    }
}
```

**关键要点**：
- 每个测试方法只测**一件事**（单一职责）
- 正常情况 → 边界情况 → 异常情况，覆盖三种场景
- 参数化测试减少重复代码

---

## 9. 常见错误排查

| 现象 | 原因 | 解决 |
|------|------|------|
| `No runnable methods` | 测试方法没加 `@Test` | 加上 `@Test` |
| `@BeforeAll 方法必须是 static` | `@BeforeAll` 方法不是 static | 加上 `static` |
| 测试全部跳过 | 类名不以 `Test` 结尾 | Maven Surefire 默认只执行 `*Test.java` |
| IDEA 中无法运行 | 测试类不在 `src/test/java` 下 | 检查目录结构 |

---

## 10. 小结

| 概念 | 一句话 |
|------|--------|
| `@Test` | 标记测试方法 |
| `@BeforeEach` | 每个测试前运行 |
| `@DisplayName` | 给测试起个好名字 |
| `assertEquals` | 断言相等 |
| `assertThrows` | 断言抛异常 |
| `@ParameterizedTest` | 一组数据跑多次 |
| 命名规范 | `should_做什么_when_什么条件` |

---

## 下一步

现在你掌握了 JUnit 5 的核心概念。下一章学习 **[02 - AssertJ 断言指南](02-assertj-guide.md)**，你会看到比 JUnit 自带断言更优雅的写法！

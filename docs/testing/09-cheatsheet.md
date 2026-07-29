# 09 — 速查表

> 本章是手册的**快速索引**。忘记某个注解或方法时，Ctrl+F 搜这里。

---

## JUnit 5 注解速查

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Test` | 标记测试方法 | `@Test void should_xxx() {}` |
| `@BeforeEach` | 每个测试前执行 | 初始化测试数据 |
| `@AfterEach` | 每个测试后执行 | 清理资源 |
| `@BeforeAll` | 所有测试前执行一次 | `static` 方法，初始化连接池 |
| `@AfterAll` | 所有测试后执行一次 | `static` 方法，关闭连接池 |
| `@DisplayName("描述")` | 给测试起个可读名字 | 支持中文 |
| `@ParameterizedTest` | 参数化测试（多组数据） | 替代复制粘贴 |
| `@ValueSource` | 参数化测试的数据源 | `strings = {"a","b"}` |
| `@CsvSource` | CSV 格式多参数 | `{"1, 2, 3"}` |
| `@NullAndEmptySource` | 传入 null 和空字符串 | — |
| `@TestMethodOrder` | 指定测试执行顺序 | `@Order(n)` |

---

## AssertJ 断言速查

```java
import static org.assertj.core.api.Assertions.*;

// 基本
assertThat(actual).isEqualTo(expected);
assertThat(actual).isNotEqualTo(expected);
assertThat(actual).isNull();
assertThat(actual).isNotNull();
assertThat(actual).isSameAs(reference);         // 同一个引用
assertThat(condition).isTrue();
assertThat(condition).isFalse();

// 字符串
assertThat(str).startsWith("prefix");
assertThat(str).endsWith("suffix");
assertThat(str).contains("sub");
assertThat(str).isNotEmpty();
assertThat(str).hasSize(n);
assertThat(str).matches("regex");

// 数字
assertThat(num).isPositive();
assertThat(num).isNegative();
assertThat(num).isZero();
assertThat(num).isGreaterThan(n);
assertThat(num).isLessThan(n);
assertThat(num).isBetween(start, end);

// 集合
assertThat(list).hasSize(n);
assertThat(list).isEmpty();
assertThat(list).isNotEmpty();
assertThat(list).contains(elem);
assertThat(list).containsExactly(e1, e2);       // 顺序严格一致
assertThat(list).doesNotContain(elem);
assertThat(list).first().isEqualTo(e1);
assertThat(list).last().isEqualTo(e2);
assertThat(list).anyMatch(it -> ...);
assertThat(list).allMatch(it -> ...);
assertThat(list).extracting(Getter).contains(...);

// Map
assertThat(map).containsKey(key);
assertThat(map).containsValue(val);
assertThat(map).hasSize(n);

// 异常
assertThatThrownBy(() -> code)
    .isInstanceOf(Ex.class)
    .hasMessage("msg")
    .hasMessageContaining("keyword");

assertThatCode(() -> code).doesNotThrowAnyException();

// Optional
assertThat(optional).isPresent();
assertThat(optional).isEmpty();
assertThat(optional).contains(value);           // 等价于 isPresent() + get().isEqualTo()

// 自定义消息
assertThat(result).as("自定义描述，%s", param).isEqualTo(expected);
```

---

## Mockito 速查

```java
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

// 基本 Mock
@Mock private Dependency dep;                   // 创建替身
@InjectMocks private Service service;           // 注入替身

// 编排（stubbing）
when(mock.method()).thenReturn(value);
when(mock.method()).thenThrow(ex);
when(mock.method()).thenReturn(v1, v2);          // 连续返回值
when(mock.method(any())).thenAnswer(inv -> inv.getArgument(0));
doReturn(value).when(mock).voidMethod();         // void 方法
doThrow(ex).when(mock).voidMethod();
doNothing().when(mock).voidMethod();             // void 方法不做事

// 参数匹配器
any(Type.class)                                  // 任意同类型值
anyString()                                      // 任意字符串
anyInt()                                         // 任意整数
eq(value)                                        // 精确匹配（配合其他匹配器）
isNull()                                         // null
isNotNull()                                      // 非 null

// 验证
verify(mock).method(args);                       // 验证被调用 1 次
verify(mock, times(n)).method(args);             // 验证被调用 n 次
verify(mock, atLeastOnce()).method(args);        // 至少 1 次
verify(mock, atLeast(n)).method(args);           // 至少 n 次
verify(mock, atMost(n)).method(args);            // 最多 n 次
verify(mock, never()).method(args);             // 从未被调用
verifyNoMoreInteractions(mock);                  // 没有其他调用

// 参数捕获
@Captor private ArgumentCaptor<Type> captor;
verify(mock).method(captor.capture());
Type captured = captor.getValue();

// lenient（避免不必要的 stub 警告）
lenient().when(mock.method()).thenReturn(value);

// Spy（部分模拟）
@Spy private RealService spyService;
doReturn(mockValue).when(spyService).someMethod();
```

---

## Spring Test 注解速查

| 注解 | 加载什么 | 速度 | Mock 方式 |
|------|----------|:--:|------|
| 无（纯 JUnit） | 无 | ⚡⚡⚡ | `@Mock` |
| `@ExtendWith(MockitoExtension.class)` | 无 | ⚡⚡⚡ | `@Mock` |
| `@DataJpaTest` | JPA 相关 | ⚡⚡ | `@MockBean` |
| `@WebMvcTest(Controller.class)` | Web 层 | ⚡⚡ | `@MockBean` |
| `@SpringBootTest` | 完整上下文 | ⚡ | `@MockBean` / `@SpyBean` |
| `@AutoConfigureMockMvc` | + MockMvc | — | 配合 `@SpringBootTest` |
| `@Transactional` | 自动回滚 | — | 配合 `@SpringBootTest` |
| `@WithMockUser` | 模拟认证 | — | 配合 `@WebMvcTest` |
| `@ActiveProfiles("test")` | 指定 profile | — | — |

---

## MockMvc 速查

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 请求方法
get("/path")
post("/path")
put("/path/{id}", id)
delete("/path/{id}", id)
patch("/path/{id}", id)

// 请求配置
.header("Key", "Value")
.contentType(MediaType.APPLICATION_JSON)
.content(jsonString)
.param("key", "value")                               // Query String

// 状态码断言
.andExpect(status().isOk())                          // 200
.andExpect(status().isCreated())                     // 201
.andExpect(status().isNoContent())                   // 204
.andExpect(status().isBadRequest())                  // 400
.andExpect(status().isUnauthorized())                // 401
.andExpect(status().isForbidden())                   // 403
.andExpect(status().isNotFound())                    // 404

// JSON 断言
.andExpect(jsonPath("$.field").value(expected))
.andExpect(jsonPath("$.nested.field").value(expected))
.andExpect(jsonPath("$.array[0]").value(expected))
.andExpect(jsonPath("$.array.length()").value(n))
.andExpect(jsonPath("$.field").exists())
.andExpect(jsonPath("$.field").doesNotExist())
.andExpect(jsonPath("$.field").isNumber())
.andExpect(jsonPath("$.field").isBoolean())
.andExpect(jsonPath("$.field").isString())

// Header 断言
.andExpect(header().string("Content-Type", "application/json"))
.andExpect(header().exists("X-Custom-Header"))

// 获取响应内容
.andReturn().getResponse().getContentAsString()
```

---

## Maven 命令速查

```bash
# 运行全部测试
mvn test

# 运行指定模块的测试
mvn test -pl domain

# 运行指定测试类
mvn test -pl domain -Dtest=EmailTest

# 运行指定测试方法
mvn test -pl domain -Dtest=EmailTest#should_createEmail

# 跳过测试
mvn package -DskipTests

# 生成覆盖率报告（需先运行 test）
mvn test jacoco:report
# 报告位置：{module}/target/site/jacoco/index.html

# 运行测试但遇到失败继续
mvn test -DfailIfNoTests=false

# 只编译测试，不执行
mvn test-compile

# IDEA 中运行单个测试
# 右键测试方法 → Run 'xxxTest.should_xxx()'
# 快捷键：Ctrl+Shift+F10
```

---

## 测试命名速查

```
公式：should_做什么_when_什么条件

✅ should_createUser
✅ should_throwException_when_emailExists
✅ should_returnEmpty_when_idNotFound
✅ should_validateValidToken
✅ should_rejectToken_when_usernameMismatch
✅ should_toggleEnabled
✅ should_convertPOToDomain
✅ should_return404_when_notFound

❌ test1
❌ testCreate
❌ verifyEmail
```

---

## 测试文件目录速查

```
src/
├── main/java/com/wsf/domain/model/user/Email.java
└── test/java/com/wsf/domain/model/user/EmailTest.java
                ↑ 相同的包路径                    ↑ 类名 + Test

src/
├── main/java/com/wsf/app/service/impl/UserServiceImpl.java
└── test/java/com/wsf/app/service/impl/UserServiceImplTest.java
```

---

## 常见错误速查

| 错误 | 原因 | 解决 |
|------|------|------|
| `No runnable methods` | 测试方法没有 `@Test` | 加上 `@Test` |
| `@BeforeAll must be static` | `@BeforeAll` 方法不是 `static` | 加上 `static` |
| `NullPointerException on @Mock` | 忘记 `@ExtendWith(MockitoExtension.class)` | 加上注解 |
| `UnnecessaryStubbingException` | Mockito 检测到未使用的 stub | 用 `lenient().when()` 或在 `@BeforeEach` 设置 |
| `Wanted but not invoked` | `verify()` 的方法没被调用 | 检查逻辑是否走了预期分支 |
| `ApplicationContext load failure` | 配置有问题或依赖缺失 | 检查 `@SpringBootTest` 的 profile 和依赖 |
| `No qualifying bean of type` | `@MockBean` 的类型没有对应的 Bean | 检查类是否正确配置为 Spring Bean |

---

## 快速决策：该用哪种测试？

```
被测对象是值对象/聚合根（纯逻辑）？
  → 纯单元测试：JUnit 5 + AssertJ，不需要 Mockito

被测对象是应用服务（依赖接口）？
  → @ExtendWith(MockitoExtension.class) + @Mock + @InjectMocks

被测对象是 Controller？
  → @WebMvcTest(Controller.class) + MockMvc + @MockBean

被测对象是 JPA Repository 的查询方法？
  → @DataJpaTest + TestEntityManager

需要测试完整请求-响应流程？
  → @SpringBootTest + @AutoConfigureMockMvc + @Transactional
```

---

> 📖 回到手册首页：[00 - 导航页](00-README.md)

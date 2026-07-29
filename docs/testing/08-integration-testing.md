# 08 — 集成测试实战

> 预计阅读：20 分钟 | 难度：★★★★★ | 对应：全栈

---

## 1. 什么是集成测试？

前面章节的测试都是**隔离的**——Mock 了所有依赖。集成测试则**启动真实组件**一起测试：

```
单元测试：Controller → Mock Service
集成测试：Controller → 真实 Service → 真实 Repository → 真实数据库
```

**何时需要集成测试？**
- 验证 JPA 查询是否正确
- 验证事务是否回滚
- 验证 Spring Security 过滤器链
- 验证完整 API 流程（注册 → 登录 → 访问保护资源）

---

## 2. `@SpringBootTest` — 启动完整容器

```java
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest                                          // 启动完整的 Spring 应用上下文
@DisplayName("用户模块集成测试")
class UserIntegrationTest {

    @Autowired
    private UserService userService;                     // 真实的 Service
    @Autowired
    private UserRepository userRepository;               // 真实的 Repository

    @Test
    @DisplayName("应能完成完整的创建→查询→删除流程")
    void should_completeFullLifecycle() {
        // 创建
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstName("王");
        req.setLastName("五");
        req.setEmail("wangwu@example.com");
        UserDto created = userService.createUser(req);
        assertThat(created.getId()).isNotNull();

        // 查询
        UserDto found = userService.findById(created.getId()).orElseThrow();
        assertThat(found.getFullName()).isEqualTo("王五");

        // 删除
        userService.deleteUser(created.getId());
        assertThat(userService.findById(created.getId())).isEmpty();
    }
}
```

**注意**：集成测试会**真实地**读写数据库！

---

## 3. `@Transactional` — 测试后自动回滚

在测试类上添加 `@Transactional`，每个测试方法结束后自动回滚数据库操作：

```java
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional                                            // ← 每个测试后自动回滚
@DisplayName("用户模块集成测试（自动回滚）")
class UserIntegrationTest {

    @Test
    void should_createUser() {
        userService.createUser(req);
        // 测试结束后，数据库中不会有这条数据
    }
}
```

**优点**：测试之间互不影响，不需要手动清理数据。

**局限**：无法测试事务提交后的行为（如异步事件、消息队列）。

---

## 4. `@AutoConfigureMockMvc` — 全栈 HTTP 测试

结合 `@SpringBootTest` 和 `MockMvc` 做全栈测试：

```java
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc                                      // ← 自动配置 MockMvc
@Transactional
@DisplayName("API 全栈集成测试")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("完整的注册→登录→访问流程")
    void should_completeRegistrationAndLogin() throws Exception {
        // 1. 注册
        String registerJson = """
            {
                "username": "newuser",
                "password": "Password123!",
                "email": "newuser@example.com"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andExpect(status().isCreated());

        // 2. 登录获取 Token
        String loginJson = """
            {
                "username": "newuser",
                "password": "Password123!"
            }
            """;

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // 提取 Token
        AuthenticateResponse authResp = objectMapper.readValue(response,
            AuthenticateResponse.class);

        // 3. 用 Token 访问保护接口
        mockMvc.perform(get("/api/protected-resource")
                .header("Authorization", "Bearer " + authResp.getAccessToken()))
            .andExpect(status().isOk());
    }
}
```

---

## 5. 测试数据库的三种策略

| 策略 | 配置 | 适用场景 |
|------|------|----------|
| **H2 内存库** | classpath 有 H2，配置自动切换 | CI/CD，不需要本地数据库 |
| **真实数据库 + 回滚** | `@Transactional` | 开发阶段，利用已有数据库 |
| **Testcontainers** | Docker 启动临时数据库 | 最接近生产环境 |

### 策略 1：H2 内存库（推荐起步）

```xml
<!-- pom.xml 添加 test 范围的 H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

```yaml
# src/test/resources/application.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate.ddl-auto: create-drop
```

### 策略 2：使用测试 profile

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/open_test   # 独立的测试库
    username: test
    password: test
```

```java
@SpringBootTest
@ActiveProfiles("test")                                   // ← 激活 test profile
@Transactional
class IntegrationTest { ... }
```

### 策略 3：Testcontainers（生产级）

```java
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DatabaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

> ⚠️ Testcontainers 需要 Docker 环境。

---

## 6. 启动类测试 — `@SpringBootTest` 验证上下文

```java
// start/src/test/java/.../StartApplicationTests.java

@SpringBootTest
class StartApplicationTests {

    @Test
    @DisplayName("应用上下文应加载成功")
    void contextLoads() {
        // 空方法：只要 Spring 容器启动成功，测试就通过
    }
}
```

这就是 Spring Initializr 生成的那个默认测试。它验证：
- 所有 Bean 能正确创建
- 配置文件能正确加载
- 模块依赖关系正确

---

## 7. `@SpyBean` — 在集成测试中部分 Mock

有时候你想用真实的 Bean，但覆盖其中某几个方法：

```java
@SpringBootTest
class IntegrationTest {

    @SpyBean                                                 // ← 真实 Bean，可以部分 Mock
    private NotificationService notificationService;

    @Test
    void should_notActuallySendEmail() {
        // 覆盖 sendEmail 方法（不发真实邮件）
        doNothing().when(notificationService).sendEmail(any());

        userService.registerUser(req);
        // 验证 sendEmail 被调用了，但实际没发邮件
        verify(notificationService).sendEmail(any());
    }
}
```

---

## 8. 集成测试的执行速度优化

集成测试比单元测试慢（需要启动 Spring 容器），优化建议：

```java
// 技术 1：缓存 Spring 上下文（同配置的测试类共享）
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestA { ... }

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestB { ... }   // TestA 和 TestB 共享同一个 Spring 上下文！

// 技术 2：父类统一配置
@SpringBootTest
@Transactional
abstract class BaseIntegrationTest { ... }    // 子类不需要重复注解

// 技术 3：按需加载（只加载需要的 Bean）
@SpringBootTest(classes = {UserService.class, UserRepository.class})
class MinimalIntegrationTest { ... }
```

---

## 9. 小结

| 注解 | 启动什么 | 速度 | 何时用 |
|------|----------|:--:|------|
| `@ExtendWith(MockitoExtension.class)` | 无 | ⚡ | 单元测试：领域层、应用层 |
| `@DataJpaTest` | JPA 相关 Bean | 🔶 | Repository 查询测试 |
| `@WebMvcTest` | Web 层 Bean | 🔶 | Controller 测试 |
| `@SpringBootTest` | 完整上下文 | 🐢 | 集成测试、全流程验证 |
| `@Transactional` | — | — | 测试后自动回滚数据库 |

**90% 的测试应该是单元测试（⚡），10% 是集成测试（🐢）。**

---

## 下一步

实战篇完成！最后一章是 **[09 - 速查表](09-cheatsheet.md)** — 所有注解、方法、命令的快速索引。

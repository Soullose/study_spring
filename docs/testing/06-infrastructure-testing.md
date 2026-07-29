# 06 — 基础设施层测试实战

> 预计阅读：30 分钟 | 难度：★★★★☆ | 对应模块：`infrastructure/`

---

## 1. 基础设施层的特点

基础设施层包含：
- **Converter**：PO（持久化对象）↔ Domain（领域对象）转换
- **JPA Repository**：数据库访问
- **Security**：JWT 生成/验证、过滤器、认证提供者
- **工具类**：`SpringUtil`、`RedisUtil`、`IpUtils` 等

测试策略分三种：

| 测试类型 | 适用对象 | 需要 Spring 容器？ | 需要数据库？ |
|----------|----------|:---:|:---:|
| 纯单元测试 | Converter、JWT 服务、工具类 | ❌ | ❌ |
| 切片测试 | JPA Repository | ✅ `@DataJpaTest` | ✅（H2 内存库） |
| 复杂集成测试 | Security 过滤器链 | ✅ `@SpringBootTest` | 按需 |

本章重点讲**前两种**，集成测试在第 08 章。

---

## 2. Converter 测试 — 以 `JwtServiceTest` 为例

Converter 是最简单的基础设施测试——不依赖 Spring，可以直接 `new`。

### 范例 1：`JwtServiceTest` — JWT Token 生成和验证

```java
// infrastructure/src/test/java/.../security/service/JwtServiceTest.java

package com.wsf.infrastructure.security.service;

import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.infrastructure.persistence.entity.user.UserAccountPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService 单元测试")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();    // 直接 new，不依赖 Spring！
    }

    @Test
    @DisplayName("应生成JWT Token")
    void should_generateToken() {
        UserAccountPO account = UserAccountPO.builder()
            .username("admin")
            .password("password")
            .enabled(true)
            .build();
        UserAccountDetail userDetails = new UserAccountDetail(account);

        String token = jwtService.generateAccessToken(userDetails);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("应提取用户名")
    void should_extractUsername() {
        UserAccountPO account = UserAccountPO.builder()
            .username("testuser")
            .password("password")
            .enabled(true)
            .build();
        UserAccountDetail userDetails = new UserAccountDetail(account);

        String token = jwtService.generateAccessToken(userDetails);
        String extracted = jwtService.extractUsername(token);

        assertThat(extracted).isEqualTo("testuser");
    }

    @Test
    @DisplayName("应验证有效Token")
    void should_validateValidToken() {
        UserAccountPO account = UserAccountPO.builder()
            .username("validuser")
            .password("password")
            .enabled(true)
            .build();
        UserAccountDetail userDetails = new UserAccountDetail(account);

        String token = jwtService.generateAccessToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("应拒绝无效Token（用户名不匹配）")
    void should_rejectToken_when_usernameMismatch() {
        UserAccountPO accountA = UserAccountPO.builder()
            .username("userA")
            .password("password")
            .enabled(true)
            .build();
        UserAccountPO accountB = UserAccountPO.builder()
            .username("userB")
            .password("password")
            .enabled(true)
            .build();

        String token = jwtService.generateAccessToken(new UserAccountDetail(accountA));
        boolean isValid = jwtService.isTokenValid(token, new UserAccountDetail(accountB));

        assertThat(isValid).isFalse();
    }
}
```

**要点**：
- `JwtService` 是一个纯业务类，直接用 `new` 创建
- 不需要 Spring 容器，运行极快
- 覆盖正向（正常生成/验证）和反向（用户名不匹配）

### 范例 2：Converter 测试模式

从本项目的 Converter 测试中提取的模式：

```java
@DisplayName("UserConverter 转换器测试")
class UserConverterTest {

    // ① 创建 Converter 实例（如果是接口就用匿名实现，如果是类就用实现类）
    private final UserConverter converter = new UserConverterImpl();

    @Test
    @DisplayName("应转换 PO → Domain")
    void should_convertPOToDomain() {
        // Given：创建 PO 对象
        UserPO po = new UserPO();
        po.setId("U001");
        po.setFirstname("张");
        po.setLastname("三");
        po.setEmail("zhangsan@example.com");

        // When：转换
        User domain = converter.toDomain(po);

        // Then：验证所有字段
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo("U001");
        assertThat(domain.getUserName().getFullName()).isEqualTo("张三");
    }

    @Test
    @DisplayName("应返回null when PO为null")
    void should_returnNull_when_POisNull() {
        assertThat(converter.toDomain(null)).isNull();
    }
}
```

**Converter 测试的标准 Checklist**：
- [ ] PO → Domain 转换：所有字段正确
- [ ] Domain → PO 转换：所有字段正确
- [ ] null 输入：返回 null
- [ ] 集合转换：空集合 → 空集合，元素逐项正确

---

## 3. JPA Repository 测试 — `@DataJpaTest`

JPA Repository 需要测试 SQL 查询。但不想污染真实数据库 → 用 H2 内存数据库或 `@DataJpaTest`。

### `@DataJpaTest` 是什么？

```java
@DataJpaTest   // ← 只加载 JPA 相关 Bean（Entity、Repository），不加载整个 Spring 上下文
```

特点：
- 自动配置 H2 内存数据库（如果 classpath 有 H2）
- 自动扫描 `@Entity` 和 JPA Repository
- 每个测试方法自动**回滚**（`@Transactional`）
- 比 `@SpringBootTest` 快很多

### 如何写一个 Repository 测试？

```java
package com.wsf.infrastructure.persistence.repository;

import com.wsf.infrastructure.persistence.entity.user.UserPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest   // ← 关键注解
@DisplayName("UserPORepository JPA 测试")
class UserPORepositoryTest {

    @Autowired
    private UserPORepository repository;     // 真实 Repository

    @Autowired
    private TestEntityManager em;            // 辅助：手动管理 Entity

    @Test
    @DisplayName("应通过邮箱查询用户")
    void should_findByEmail() {
        // Given: 直接插入测试数据
        UserPO user = UserPO.builder()
            .id("U001")
            .firstname("张")
            .lastname("三")
            .email("zhangsan@example.com")
            .build();
        em.persist(user);
        em.flush();                          // 立即同步到数据库

        // When
        Optional<UserPO> result = repository.findByEmail("zhangsan@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getFirstname()).isEqualTo("张");
    }

    @Test
    @DisplayName("应返回空 when 邮箱不存在")
    void should_returnEmpty_when_emailNotFound() {
        Optional<UserPO> result = repository.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }
}
```

**要点**：
- `TestEntityManager` 可以在测试中直接操作 JPA，比调用 Service 更直接
- 测试结束后数据自动回滚，不污染数据库

### 当前项目没有 H2 依赖？

可以添加：
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

或者使用 `@SpringBootTest` + 真实数据库（见第 08 章）。

---

## 4. Spring 上下文依赖的测试

有些基础设施类需要 Spring 容器中的配置（比如 `application-security.yml` 中的 `jwt.secret`）。

### 方式 1：手动构造（像 `JwtServiceTest` 那样）

如果被测类可以在 `@BeforeEach` 中手动创建，优先使用这种方式——最快、最可控。

### 方式 2：用 `@SpringBootTest` 加载上下文

当类依赖了 Spring 管理的配置时：

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest   // 加载完整的 Spring 上下文
@DisplayName("JwtService 集成测试")
class JwtServiceIntegrationTest {

    @Autowired
    private JwtService jwtService;    // 从 Spring 容器中获取（带有真实配置）

    @Test
    @DisplayName("应用配置中的密钥应正确加载")
    void should_useConfiguredSecret() {
        String token = jwtService.generateAccessToken(details);
        assertThat(token).isNotNull();
    }
}
```

> ⚠️ `@SpringBootTest` 较慢（启动 Spring 容器），能不用就不用。详见第 08 章。

---

## 5. Security 组件测试的特殊考虑

Security 相关组件（过滤器、Provider、Handler）通常依赖 `HttpServletRequest` / `HttpServletResponse`，测试需要额外处理。

### 测试 Security Filter

```java
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationTokenFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserAccountDetailService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationTokenFilter filter;

    @Test
    @DisplayName("应跳过无Authorization头的请求")
    void should_skip_when_noAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        // 验证：filterChain 继续执行，没有设置认证
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    @DisplayName("应认证有效Token")
    void should_authenticate_when_validToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtService.extractUsername("valid.token.here")).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin"))
            .thenReturn(mockUserDetails());
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        // 验证：SecurityContext 中设置了认证
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService).loadUserByUsername("admin");
    }
}
```

**关键技巧**：
- `HttpServletRequest` / `HttpServletResponse` 都是接口 → 用 `@Mock`
- `FilterChain` 也用 `@Mock` → 验证 `doFilter` 是否被调用
- 断言重点：认证是否被正确设置到 `SecurityContextHolder` 中

### 测试结束后清理 SecurityContext

```java
@AfterEach
void tearDown() {
    SecurityContextHolder.clearContext();      // 避免测试间污染
}
```

---

## 6. 工具类测试 — 以 `IpUtilsTest` 为例

```java
@DisplayName("IpUtils 单元测试")
class IpUtilsTest {

    @Test
    @DisplayName("应获取本机IPv4地址")
    void should_getLocalIp() {
        String ip = IpUtils.getLocalIp();
        assertThat(ip).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("应判断内网IP")
    void should_detectInternalIp() {
        assertThat(IpUtils.isInternalIp("192.168.1.1")).isTrue();
        assertThat(IpUtils.isInternalIp("10.0.0.1")).isTrue();
        assertThat(IpUtils.isInternalIp("8.8.8.8")).isFalse();
    }
}
```

工具类通常没有外部依赖 → 直接 `new` 或调静态方法。

---

## 7. 基础设施测试 `pom.xml` 依赖

```xml
<!-- infrastructure 模块需要测试 Spring 相关的 Bean -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- 如果用 H2 做 JPA 测试 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 8. 本模块已有测试

| 测试类 | 类型 | 状态 |
|--------|------|:--:|
| `JwtServiceTest` | 纯单元测试 | ✅ |
| `JwtUtilTest` | 工具类测试 | ❓ |
| `IpUtilsTest` | 工具类测试 | ❓ |
| `ResponseUtilsTest` | 工具类测试 | ❓ |
| `ResultTest` | 值对象测试 | ❓ |
| `DataPermissionConverterTest` | Converter | ❓ |
| `MenuConverterTest` | Converter | ❓ |
| `RoleConverterTest` | Converter | ❓ |
| `UserConverterTest` | Converter | ⚠️ 编译错误 |
| `UserAccountConverterTest` | Converter | ⚠️ 编译错误 |
| `JwtAuthenticationTokenFilterTest` | Filter | ❓ |
| `LoginFilterTest` | Filter | ❓ |
| `AuthenticationServiceTest` | Service | ❓ |

> ⚠️ `UserConverterTest` 和 `UserAccountConverterTest` 引用了旧的 PO 类名（`User` / `UserAccount`）需要改为 `UserPO` / `UserAccountPO`。

---

## 9. 小结

| 测试类型 | 注解 | 需要什么 | 速度 |
|----------|------|----------|:--:|
| 纯单元测试 | `@ExtendWith(MockitoExtension.class)` | 无 | ⚡ |
| Converter 测试 | 无 | 无 | ⚡ |
| JPA Repository 测试 | `@DataJpaTest` | H2 依赖 | 🔶 |
| Security Filter 测试 | Mockito | Mock HttpServletRequest | ⚡ |
| 集成测试 | `@SpringBootTest` | 完整上下文 | 🐢 |

---

## 下一步

下一章学习 **[07 - 控制器层测试实战](07-controller-testing.md)** — 用 `MockMvc` 测试 REST API！

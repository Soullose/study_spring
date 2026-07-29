# 07 — 控制器层测试实战

> 预计阅读：25 分钟 | 难度：★★★★☆ | 对应模块：`rest/`

---

## 1. 控制器测试的目标

控制器（Controller）是 HTTP 请求的入口。测试目的是验证：

| 验证项 | 示例 |
|--------|------|
| URL 映射 | `POST /api/users` 是否正确路由？ |
| 请求参数绑定 | `@RequestBody`、`@PathVariable` 是否正确解析？ |
| HTTP 状态码 | 成功返回 200？参数错误返回 400？ |
| 响应体 JSON | 返回的 JSON 结构和值是否正确？ |
| 安全控制 | 未登录用户访问保护接口是否返回 401？ |

---

## 2. 核心工具：`MockMvc`

`MockMvc` 是 Spring Test 提供的工具，可以**不启动真实 HTTP 服务器**的情况下模拟 HTTP 请求。

```java
mockMvc.perform(
    get("/api/users/123")              // 发起 GET 请求
        .header("Authorization", token) // 设置 Header
)
.andExpect(status().isOk())            // 断言 HTTP 200
.andExpect(jsonPath("$.name").value("张三"));  // 断言 JSON 中的字段
```

---

## 3. `@WebMvcTest` — 轻量级控制器测试

```java
@WebMvcTest(StudyController.class)     // ← 只加载指定的 Controller
```

特点：
- 只加载 Web 层（Controller、Filter、`@ControllerAdvice`）
- **不加载** Service、Repository（需要手动 Mock）
- 自动配置 `MockMvc`
- 比 `@SpringBootTest` 快得多

### 完整范例

```java
package com.wsf.controller;

import com.wsf.api.service.UserService;
import com.wsf.api.dto.user.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudyController.class)          // ① 指定测试哪个 Controller
@DisplayName("StudyController Web层测试")
class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;                 // ② 注入 MockMvc

    @MockBean                                 // ③ Mock Service（替代 @Mock）
    private UserService userService;          //    Spring 容器会把这个 Mock 注入 Controller

    @Test
    @DisplayName("GET /api/users/{id} 应返回用户JSON")
    void should_returnUser_when_found() throws Exception {
        // Given：准备 Service 层返回
        UserDto dto = new UserDto();
        dto.setId("U001");
        dto.setFirstName("张");
        dto.setLastName("三");
        when(userService.findById("U001")).thenReturn(Optional.of(dto));

        // When + Then：执行请求并断言
        mockMvc.perform(get("/api/users/U001"))
            .andExpect(status().isOk())                    // HTTP 200
            .andExpect(jsonPath("$.id").value("U001"))     // JSON 中 id = "U001"
            .andExpect(jsonPath("$.firstName").value("张"))
            .andExpect(jsonPath("$.lastName").value("三"));
    }

    @Test
    @DisplayName("GET /api/users/{id} 应返回404 when 用户不存在")
    void should_return404_when_notFound() throws Exception {
        when(userService.findById("NONEXIST")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/NONEXIST"))
            .andExpect(status().isNotFound());
    }
}
```

---

## 4. `@MockBean` vs `@Mock`

| 注解 | 用途 | 谁管理？ |
|------|------|----------|
| `@Mock` | Mockito 原生，创建替身对象 | Mockito |
| `@MockBean` | Spring Boot 提供，创建替身并**放入 Spring 容器** | Spring |

**在 `@WebMvcTest` 中必须用 `@MockBean`**，因为 Controller 的依赖是从 Spring 容器获取的。

---

## 5. MockMvc 请求构建大全

### HTTP 方法

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

get("/api/users")           // GET
post("/api/users")          // POST
put("/api/users/123")       // PUT
delete("/api/users/123")    // DELETE
patch("/api/users/123")     // PATCH
```

### 设置 Header

```java
get("/api/users")
    .header("Authorization", "Bearer token-here")
    .header("Accept-Language", "zh-CN")
```

### 设置请求参数

```java
// Query String: /api/users?page=1&size=10
get("/api/users")
    .param("page", "1")
    .param("size", "10")

// Path Variable: /api/users/123
get("/api/users/{id}", "123")
```

### 发送 JSON 请求体

```java
post("/api/users")
    .contentType(MediaType.APPLICATION_JSON)
    .content("""
        {
            "firstName": "张",
            "lastName": "三",
            "email": "zhangsan@example.com"
        }
        """)
```

或者用 Jackson 序列化对象：

```java
@Autowired
private ObjectMapper objectMapper;

CreateUserRequest req = new CreateUserRequest();
req.setFirstName("张");

post("/api/users")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(req))
```

---

## 6. MockMvc 断言大全

### 状态码

```java
.andExpect(status().isOk())              // 200
.andExpect(status().isCreated())         // 201
.andExpect(status().isNoContent())       // 204
.andExpect(status().isBadRequest())      // 400
.andExpect(status().isUnauthorized())    // 401
.andExpect(status().isForbidden())       // 403
.andExpect(status().isNotFound())        // 404
```

### JSON 路径断言（jsonPath）

```java
// 基本取值
jsonPath("$.id").value("U001")           // JSON 字段等于某值
jsonPath("$.name").value("张三")

// 嵌套对象
jsonPath("$.address.city").value("北京")

// 数组
jsonPath("$.tags[0]").value("admin")     // 数组第一个元素
jsonPath("$.tags").isArray()             // 是数组
jsonPath("$.tags.length()").value(3)     // 数组长度

// 存在性
jsonPath("$.id").exists()                // 字段存在
jsonPath("$.password").doesNotExist()    // 字段不存在

// 类型检查
jsonPath("$.age").isNumber()
jsonPath("$.active").isBoolean()
jsonPath("$.name").isString()
```

### 响应 Header

```java
.andExpect(header().string("Content-Type", "application/json"))
.andExpect(header().exists("X-Request-Id"))
```

---

## 7. 测试 POST 请求（创建资源）

```java
@Test
@DisplayName("POST /api/users 应创建用户并返回201")
void should_createUser() throws Exception {
    CreateUserRequest req = new CreateUserRequest();
    req.setFirstName("李");
    req.setLastName("四");
    req.setEmail("lisi@example.com");

    UserDto saved = new UserDto();
    saved.setId("U002");
    saved.setFirstName("李");
    when(userService.createUser(any())).thenReturn(saved);

    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())                       // 201 Created
        .andExpect(jsonPath("$.id").value("U002"))
        .andExpect(header().exists("Location"));               // 有些 API 会返回 Location 头
}
```

---

## 8. 测试 DELETE 请求

```java
@Test
@DisplayName("DELETE /api/users/{id} 应删除用户并返回204")
void should_deleteUser() throws Exception {
    doNothing().when(userService).deleteUser("U001");

    mockMvc.perform(delete("/api/users/U001"))
        .andExpect(status().isNoContent());                    // 204 No Content

    verify(userService).deleteUser("U001");
}
```

---

## 9. 测试带认证的请求

当 Controller 受 Spring Security 保护时：

```java
import org.springframework.security.test.context.support.WithMockUser;

@Test
@WithMockUser(username = "admin", roles = {"ADMIN"})          // ← 模拟已登录管理员
@DisplayName("管理员应能查看所有用户")
void should_listUsers_when_admin() throws Exception {
    when(userService.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk());
}

@Test
@WithMockUser(username = "user", roles = {"USER"})            // ← 模拟已登录普通用户
@DisplayName("普通用户不应能删除用户")
void should_forbidden_when_normalUser() throws Exception {
    mockMvc.perform(delete("/api/users/U001"))
        .andExpect(status().isForbidden());                    // 403
}
```

> 需要依赖 `spring-security-test`：
> ```xml
> <dependency>
>     <groupId>org.springframework.security</groupId>
>     <artifactId>spring-security-test</artifactId>
>     <scope>test</scope>
> </dependency>
> ```

### 模拟 JWT Token

```java
@Test
@DisplayName("带有效JWT Token应返回200")
void should_authenticate_withJwt() throws Exception {
    String token = jwtService.generateAccessToken(userDetails);

    mockMvc.perform(get("/api/protected-resource")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
}
```

---

## 10. 测试异常处理

验证 Controller 抛出异常后的响应：

```java
@Test
@DisplayName("应返回400 when 请求参数非法")
void should_return400_when_invalidInput() throws Exception {
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                { "email": "not-an-email", "firstName": "" }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").isArray());
}
```

---

## 11. 本模块已有测试

| 测试类 | 被测 Controller |
|--------|:--:|
| `StudyControllerTest` | StudyController |
| `EventBusDemoControllerTest` | EventBusDemoController |
| `TestControllerTest` | TestController |

> ⚠️ `LoginController` **缺少测试** — 这是认证入口，建议优先补充。

---

## 12. 控制器测试 pom.xml 依赖

```xml
<!-- rest/pom.xml 需要的测试依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- 安全测试（如需 @WithMockUser） -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 13. 小结

| 要点 | 说明 |
|------|------|
| `@WebMvcTest(Controller.class)` | 只加载 Web 层，轻量快速 |
| `@MockBean` | 替代 `@Mock`，将 Mock 放入 Spring 容器 |
| `mockMvc.perform(request)` | 模拟 HTTP 请求 |
| `status().isOk()` | 断言 HTTP 状态码 |
| `jsonPath("$.field").value(x)` | 断言 JSON 字段 |
| `@WithMockUser` | 模拟已认证用户 |
| `contentType(APPLICATION_JSON)` | 设置 Content-Type |

---

## 下一步

前面都是轻量级测试。最后一章实战： **[08 - 集成测试实战](08-integration-testing.md)** — 启动完整的 Spring 容器，连接真实数据库！

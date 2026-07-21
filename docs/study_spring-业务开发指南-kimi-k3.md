# study_spring 业务开发指南（kimi-k3）

> 本文面向**在该代码库上做业务开发**的工程师，讲清楚：项目怎么分层、新增一个业务功能要在哪些模块写哪些代码、有哪些现成的机制可以直接用、有哪些坑要避开。
> 架构总览另见《study_spring-架构框架说明-kimi-k3.md》，本文侧重"怎么动手"。

---

## 1. 技术栈与运行环境

| 项 | 版本 / 说明 |
|----|------------|
| JDK | **21**（以根 `pom.xml` 为准；《启动说明.md》写 17 已过时） |
| Spring Boot | 3.5.0 |
| 持久化 | Spring Data JPA + Hibernate 6 + QueryDSL 5.1 |
| 数据库 | MySQL（默认，`open` 库）/ PostgreSQL（注释备选） |
| 缓存 | Redis（Redisson 3.44，单机 6379） |
| 安全 | Spring Security + JWT（jjwt 0.12.6），密码用 Argon2 |
| 接口文档 | Knife4j 4.5，启动后访问 `http://localhost:40001/doc.html` |
| 对象映射 | MapStruct（仅 persistence 层 converter 用） |
| 其他 | Lombok、Hutool、Guava、Resilience4j、Cosid/雪花/UUID、Quartz、MQTT、Netty WebSocket |

### 构建与启动

```bash
# 打包（根目录）
mvn clean package

# 运行（可执行 jar 在 start/target，注意是 jar+lib 目录形式，非 fat jar）
java -jar start/target/start-1.0-SNAPSHOT.jar

# 指定 profile / 端口
java -jar start-1.0-SNAPSHOT.jar --spring.profiles.active=prod --server.port=8080
```

默认端口 **40001**，应用名 `w2-server`。启动前需要：
- MySQL：建 `open` 库，账号密码见 `start/src/main/resources/application-datasource.yml`（默认 root/123456789）；`ddl-auto=update` 会自动建表。
- Redis：本地 6379，见 `application-redis.yml`。

---

## 2. 模块分层：代码该写在哪

这是一个 **DDD 分层** 的多模块项目。理解"每类代码落在哪个模块"是开发的前提。

```
┌─────────────────────────────────────────────────────────┐
│ rest          接口层：@RestController（HTTP 入口）         │
├─────────────────────────────────────────────────────────┤
│ api           契约层：Service 接口 + DTO（零实现、零依赖）   │
├─────────────────────────────────────────────────────────┤
│ app           应用层：ServiceImpl，编排用例、事务边界        │
├─────────────────────────────────────────────────────────┤
│ domain        领域层：聚合根、值对象、领域服务、仓储接口      │
├─────────────────────────────────────────────────────────┤
│ infrastructure 基础设施层：JPA 实体/仓储实现/安全/配置       │
└─────────────────────────────────────────────────────────┘
        start = 启动模块（聚合打包 + 配置文件）
        system = 历史遗留老式分层（勿在此写新代码）
        adapter / data = 基本为空，预留
```

### 各模块职责与依赖规则

| 模块 | 你该在这里写什么 | 依赖规则 |
|------|------------------|----------|
| `api` | 对外的 Service **接口**、DTO（Create/Update 请求 + 响应 Dto），带 `jakarta.validation` 校验注解 | 只依赖 validation + Lombok，**不许**依赖 Spring/domain |
| `app` | ServiceImpl 应用服务：`@Service` + `@Transactional`，编排"取聚合 → 调行为 → 保存" | 依赖 `api` + `domain` |
| `domain` | 聚合根/实体的业务方法、值对象的校验、领域服务、仓储**接口**、领域事件 | 应保持纯净，不感知 JPA/HTTP |
| `infrastructure` | JPA 实体（PO）、仓储**实现**（实现 domain 的接口）、Converter、安全、数据源、ID 生成、各种配置 | 依赖 `domain`，是依赖倒置的实现侧 |
| `rest` | `@RestController`，只做参数接收与调用 api 接口，**不写业务逻辑** | 依赖 `api` |

> **核心原则：Controller 调 api 接口，api 由 app 实现，app 编排 domain，domain 的仓储接口由 infrastructure 实现。** 上层只依赖下层抽象，不反向依赖。

---

## 3. 标准开发流程：新增一个业务功能

以"新增一个【商品 Product】的增删改查"为例，按从下往上或从上往下都可以，推荐按依赖方向自底向上。

### 第 1 步：domain 层 —— 定义领域模型与仓储接口

**聚合根**（`domain/src/main/java/com/wsf/domain/model/product/aggregate/Product.java`）：
- 用**静态工厂方法**创建（如 `Product.create(...)`），把构造规则收在聚合内。
- 业务行为写成聚合的方法（如 `product.enable()` / `product.update(...)`），而不是在外部 setter。
- 参考现有实现：`User.create(...)`、`UserAccount.lock()/unlock()`、`Menu.createDirectory/createMenu/createButton`、`Role.enable()/disable()`。

**值对象**（`domain/model/product/valueobject/`）：
- 把有校验语义的字段包装成值对象，校验逻辑放值对象内部。
- 参考：`UserName`、`Email`、`PhoneNumber`、`IdCardNumber`、`Password`、`RoleCode`（`RoleCode` 要求大写字母开头）。

**仓储接口**（`domain/repository/ProductRepository.java`）：
- 只声明接口，方法签名参考 `UserRepository`：`save`、`findById`、`findAll`、`findByIds`、`deleteById`、`existsByXxx` 等。
- 入参用值对象而非裸 String（如 `findByEmail(Email email)`）。

**领域事件**（可选，`domain/event/`）：
- 继承 `BaseDomainEvent`，参考 `UserCreatedEvent(source, userId, name, email)`。
- ⚠️ 注意：当前聚合根本身**不自动发布事件**，事件由应用层或专门的发布点通过 `ApplicationEventPublisher` 发出（见第 6 节）。

### 第 2 步：api 层 —— 定义接口契约与 DTO

**Service 接口**（`api/src/main/java/com/wsf/api/service/ProductService.java`）：
- 参考 `UserService` / `RoleService`：`createXxx`、`updateXxx`、`findById`、`findAll`、`deleteXxx`，以及业务动作（`enableXxx`/`disableXxx`）。

**DTO**（`api/dto/product/`）：
- `CreateProductRequest` / `UpdateProductRequest`：用 Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`，加校验注解。
  - 现有约定：`@NotBlank(message = "xxx不能为空")`、`@Email`、`@Pattern(regexp=..., message=...)`。
- `ProductDto`（响应）：纯数据，无校验注解，含 `createTime`/`updateTime`。

### 第 3 步：app 层 —— 实现应用服务（核心业务编排）

**ServiceImpl**（`app/src/main/java/com/wsf/app/service/impl/ProductServiceImpl.java`）：

固定范式（所有现有 Impl 都遵循）：

```java
@Service
@RequiredArgsConstructor   // Lombok 构造器注入
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;   // domain 仓储接口
    private final IdGenerator idGenerator;               // domain ID 生成器

    @Override
    @Transactional          // 写操作加事务，查询不加
    public ProductDto createProduct(CreateProductRequest request) {
        // 1. 唯一性校验（冲突抛 IllegalArgumentException）
        if (productRepository.existsByName(new ProductName(request.getName()))) {
            throw new IllegalArgumentException("名称已存在");
        }
        // 2. 生成 ID
        String id = idGenerator.generate();
        // 3. 值对象包装 + 聚合工厂创建
        Product product = Product.create(id, new ProductName(request.getName()), ...);
        // 4. 保存
        Product saved = productRepository.save(product);
        // 5. 转 DTO 返回
        return toDto(saved);
    }

    // 私有手写 toDto（项目当前统一用手写 builder，不用 MapStruct 于 app 层）
    private ProductDto toDto(Product p) { ... }
}
```

要点：
- **查询方法**：`repository.findAll().stream().map(this::toDto).toList()`。
- **业务动作方法**（启用/禁用等）：`findById → 聚合.行为() → save` 三步。
- **异常**：统一抛 `IllegalArgumentException` + 中文消息（项目现状，未定义专用业务异常）。
- 需要跨聚合时（如 Role 分配菜单），在 app 层用对应仓储把关联实体查出来再交给聚合：`menuRepository.findByIds(menuIds)` → `role.assignMenus(menus)`。

### 第 4 步：infrastructure 层 —— 持久化落地

**PO 实体**（`infrastructure/persistence/entity/product/ProductPO.java`）：
- 继承 `BaseEntity`（自带 `id_`、`create_date_`、`modify_date_` 审计字段），加 `@Entity @Table(name="T_OPEN_PRODUCT_")` + `@EntityListeners(AuditingEntityListener.class)`。
- 列名约定：下划线后缀，如 `@Column(name = "product_name_")`。

**JPA Repository**（`infrastructure/persistence/repository/ProductJpaRepository.java`）：继承 `JpaRepository`（或项目增强基座），声明派生查询如 `findByName(String)`。

**Converter**（`infrastructure/persistence/converter/ProductConverter.java`）：PO ↔ Domain 双向转换，此层**可以用 MapStruct**（参考 `UserConverter`），生成类在 `target/generated-sources`。

**仓储实现**（`infrastructure/persistence/repository/impl/ProductRepositoryImpl.java`）：

```java
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    private final ProductConverter converter;

    @Override
    public Product save(Product p) {
        var po = converter.toPO(p);
        return converter.toDomain(jpaRepository.save(po));
    }
    // findById/findAll/...：jpaRepository 查询后 .map(converter::toDomain)
}
```

### 第 5 步：rest 层 —— 暴露 HTTP 接口

**Controller**（`rest/src/main/java/com/wsf/controller/ProductController.java`）：

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;   // 只依赖 api 接口

    @PostMapping
    public Result<ProductDto> create(@Validated @RequestBody CreateProductRequest request) {
        return Result.success(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public Result<ProductDto> get(@PathVariable String id) {
        return productService.findById(id)
                .map(Result::success)
                .orElse(Result.failed("商品不存在"));
    }
}
```

- 用 `Result<T>` 包装统一响应（见第 5 节）。
- `@Validated` 触发 DTO 上的校验注解。
- Controller **只做**参数接收/校验/调用/包装，不写业务。

> ⚠️ 现状提醒：`rest` 模块现有的 `TestController`/`StudyController` 多为演示性质，真正的登录接口在 `infrastructure/security/auth/AuthenticationController`。新业务的 Controller 请放在 `rest` 模块。

---

## 4. 现成的业务服务接口（可直接调用）

以下是 `api` 模块已实现的契约，新功能可以直接注入使用。

### UserService — `api/service/UserService.java`
| 方法 | 说明 |
|------|------|
| `UserDto createUser(CreateUserRequest)` | 创建用户（可同时创建账户，`createAccount=true` 时需给 username/password） |
| `UserDto updateUser(String userId, UpdateUserRequest)` | 更新用户（⚠️ 当前未校验邮箱/手机号唯一性，不更新 idCardNumber/realName） |
| `Optional<UserDto> findById(String)` / `List<UserDto> findAll()` | 查询 |
| `void deleteUser(String userId)` | 删除 |
| `UserDto createAccountForUser(String userId, String username, String password)` | 为用户开通账户 |
| `UserDto unlinkAccount(String userId)` | 解绑账户 |

### RoleService — `api/service/RoleService.java`
创建/更新/查询/删除角色，`enableRole`/`disableRole`，`assignMenus(roleId, menuIds)`，`assignDataPermissions(roleId, permissionIds)`。

### MenuService — `api/service/MenuService.java`
菜单 CRUD，`getMenuTree()`（树形），`showMenu/hideMenu/enableMenu/disableMenu`。`menuType` 取值 `DIR/MENU/BUTTON`。

### DataPermissionService — `api/service/DataPermissionService.java`
数据权限 CRUD，`resourceType` 取值 `DEPT/ORG/CUSTOM`，`dataScope` 取值 `ALL/DEPT/DEPT_AND_BELOW/SELF/CUSTOM`（CUSTOM 时需给 `resourceIds`）。

### FileService — `api/service/FileService.java`
`extractChecksum(path/stream, algorithm)` 提取文件 MD5/SHA。⚠️ 当前 `FileServiceImpl` 是**桩实现，返回空字符串**，需自行补全。

> ⚠️ `api/service/AuthenticationService` 是**空接口**，`UserResponse` 是空类 —— 属占位，别误用。

---

## 5. 统一响应、异常与认证

### 5.1 统一响应 Result
`infrastructure/common/result/Result.java`：
- `Result.success(data)` / `Result.success()`
- `Result.failed()` / `Result.failed(msg)` / `Result.failed(IResultCode)`
- `Result.judge(boolean)`：true 返回 success，false 返回 failed
- 结构：`{ status, code, data, msg }`，错误码枚举见 `ResultCode`。

### 5.2 登录认证
- 登录入口：`POST /api/v1/auth/authenticate`（body `AuthenticateRequest`：账号+密码），成功返回 JWT。
- 注册：`POST /api/v1/auth/register`。
- 后续请求在 Header 携带 JWT，由 `JwtAuthenticationTokenFilter` 解析。
- 在业务代码中取当前登录用户：
  ```java
  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
  UserAccountDetail user = (UserAccountDetail) auth.getPrincipal();  // infrastructure.security.domain
  ```
  参考 `TestController.test/employees`。

### 5.3 接口放行（白名单）
`SecurityConfig` 中已放行：`/doc.html`、`/swagger-ui.html`、`/webjars/**`、`/v3/**`、`/swagger-resources/**`、`/test/**`、`/api/v1/auth/**`。
- **新增的对外开放接口需在 `SecurityConfig` 的 `authorizeHttpRequests` 里加 `permitAll`**，否则默认 `authenticated()`（需登录）。
- 过滤器顺序：`LoginFilter`（登录）→ `JwtAuthenticationTokenFilter`（JWT 校验）→ `UserAwareRateLimitFilter`（限流）。

---

## 6. 领域事件 / Spring Event 用法

项目已从自定义事件迁移到 **Spring Event** 标准机制。

**定义事件**：继承 `BaseDomainEvent`（`domain/event/`），参考 `UserCreatedEvent` / `RoleAssignedEvent` / `AccountLockedEvent`。

**发布事件**：
```java
private final ApplicationEventPublisher publisher;
publisher.publishEvent(new UserCreatedEvent(this, userId, userName, email));
```

**订阅事件**（参考 `system/service/UserEventHandler.java`）：
```java
@Component
public class XxxEventHandler {
    @Async("eventUserExecutor")        // 用专用线程池异步执行
    @Order(1)                          // 多个监听器的执行顺序
    @EventListener(condition = "#event.userId > 100")   // SpEL 条件过滤
    public void on(UserCreatedEvent event) { ... }
}
```

可用线程池（定义在 `infrastructure/config/SpringEventConfiguration.java`，已 `@EnableAsync`）：
- `eventUserExecutor`（用户域，核心=CPU 核数）
- `eventAccountExecutor`（账户域）
- `eventSystemExecutor`（兜底默认池）

> 演示入口：`rest` 模块 `EventBusDemoController`（`GET /api/event/publish-user-created` 等）。

---

## 7. 基础设施能力速查

| 能力 | 位置 | 业务侧怎么用 |
|------|------|--------------|
| **主键 ID** | `infrastructure/jpa/id` | PO 继承 `BaseEntity` 即可，`@BaseId` + `custom-id-generator` 自动赋值；另有雪花 `SnowflakeIdGenerator`、Cosid、多种 UUID。app 层用 `IdGenerator.generate()` |
| **审计字段** | `BaseEntity` + `jpa/audit` | `create_date_`/`modify_date_` 自动填充，无需手动 set |
| **多数据源** | `datasource/annotation/@DynamicDataSource` | 方法或类上加注解切换，支持 SpEL：`@DynamicDataSource("db2")`、`#user.dataSource`、`@bean.getDs()`；`force=true` 忽略拦截器设置。默认数据源是 `open` |
| **缓存** | Redisson | 注入 `RedissonClient`（`config/RedissonConfiguration.java`，单机 + JsonJacksonCodec） |
| **通用查询** | QueryDSL | 注入 `JPAQueryFactory`，用 `Q*` 类做类型安全查询（参考 `TestController` 注释掉的示例） |
| **定时任务** | Quartz | ⚠️ `application-quartz.yml` **未被 include**，需在 `application.yml` 的 `spring.profiles.include` 加 `quartz` 才生效；`QuartzConfiguration` 是空类，需自建 Job/Trigger |
| **MQTT** | `mqtt/MqttClientCount` | 不自动连接；自行 `new MqttClientCount().setMqttClient(host,clientId,user,pass,callback)` 后 `pub/sub`，重写 `MqttClientCallback.messageArrived` 处理消息 |
| **WebSocket** | `websocket/` | 随应用自动启动在 **8090** 端口，路径 `ws://host:8090/websocket`；⚠️ 消息处理是空壳，分发/鉴权需自行扩展 `NettyWebSocketServerHandler` |
| **文件监听** | `watchfile/` | 往 `watchfiles/`（json/zip）或 `watch/` 目录丢文件即触发；写 `@Component` 用 `@EventListener` 订阅 `WatchFileOnCreateEvent`（File）或 `FileCreatedEvent`（Path） |

---

## 8. 配置项速查（start/src/main/resources）

| 配置 | 文件 | 关键项 / 默认值 |
|------|------|----------------|
| 端口/应用名 | `application.yml` | `server.port=40001`、`spring.application.name=w2-server`；`include: jpa,datasource,openapi,redis`（注意没含 quartz） |
| 数据源 | `application-datasource.yml` | 自定义前缀 `spring.datasource.open.*`，MySQL `open` 库 root/123456789；PostgreSQL 注释备选 |
| JPA | `application-jpa.yml` | 前缀 `spring.jpa.primary.*`；`ddl-auto=update`、show-sql、MySQLDialect |
| Redis | `application-redis.yml` | `spring.data.redis` 127.0.0.1:6379，clientName `w2-server` |
| 接口文档 | `application-openapi.yml` | knife4j 开启，文档路径 `/doc.html`，api-docs `/v3/api-docs` |
| Quartz | `application-quartz.yml` | memory job-store，**默认不加载** |
| 日志 | `logback-spring.xml` | `com.wsf` 包 DEBUG，彩色控制台输出 |

---

## 9. 开发注意事项 / 避坑清单

1. **JDK 用 21**，别信《启动说明.md》里的 17（以根 `pom.xml` 为准）。
2. **新业务不要写进 `system` 模块**——那是历史遗留的老式分层，大量类已注释停用，新功能走 `domain/app/api/rest` DDD 模块。`adapter`/`data` 目前是空壳。
3. **app 层 DTO 转换统一用手写 `toDto` builder**，不要引入 MapStruct 到 app 层（MapStruct 只用在 infrastructure 的 Converter）。
4. **`UserServiceImpl.toDto` 每次会额外查一次账户**（`accountRepository.findByUserId`），列表查询有 **N+1 风险**；批量场景注意优化。
5. **聚合根不自动发事件**，需要事件时在 app 层用 `ApplicationEventPublisher` 显式发布。
6. **异常目前统一是 `IllegalArgumentException`**，全局异常处理/统一错误码映射尚未完善，Controller 层注意校验与包装。
7. **这些是不完整的占位/桩**：`AuthenticationService`（空接口）、`FileServiceImpl`（返回空串）、`QuartzConfiguration`（空类）、WebSocket 消息处理、DataPermissionServiceImpl 里未被调用的 `parseResourceIds`。用到时先补实现。
8. **新增开放接口记得加白名单**（`SecurityConfig`），否则会被拦截要求登录。
9. **Controller 位置不统一**：新业务 Controller 放 `rest`，但登录相关在 `infrastructure/security/auth`，留意别混淆。
10. **打包是 jar + lib 目录形式**（`start/pom.xml` 用 copy-dependencies），不是单 fat jar，部署时 `lib/` 要一起带上。

---

## 10. 测试规范

项目对 domain 和 app 层有较完整的单元测试，新功能应补齐对应测试。

### 分层测试范式

| 层 | 测试类型 | 写法 | 参考 |
|----|----------|------|------|
| domain | 纯单元测试（无 Spring） | JUnit5 + AssertJ，直接 new 值对象/聚合验证行为与校验 | `domain/src/test/.../UserTest.java` |
| app | Mock 单元测试 | `@ExtendWith(MockitoExtension.class)` + `@Mock` 仓储 + `@InjectMocks` ServiceImpl，验证编排与异常 | `app/src/test/.../UserServiceImplTest.java` |
| rest | 控制器测试 | `@WebMvcTest` / MockMvc | `rest/src/test/.../TestControllerTest.java` |

约定：
- 类上加 `@DisplayName("Xxx 测试")`，方法用 `@DisplayName("应...")` 中文描述。
- 断言用 AssertJ（`assertThat(...)` / `assertThatThrownBy(...)`）。
- app 层测试重点：唯一性校验抛 `IllegalArgumentException`、`verify(repository).save(...)` 交互。

### 运行与覆盖率

```bash
mvn test                 # 跑全部测试
mvn test -pl domain,app  # 只跑指定模块
```

JaCoCo 已配置（根 `pom.xml`），**覆盖率排除了** `Q*`（QueryDSL 生成类）、`api/dto/**`、`system/**`、`adapter/**`。也就是说 **domain 和 app 是被统计覆盖率的**，新写的聚合/应用服务要配测试。

---

## 11. 工具库与日志

- **Lombok**：实体/DTO 用 `@Data/@Getter/@Setter/@Builder/@NoArgsConstructor/@AllArgsConstructor`；Service 用 `@RequiredArgsConstructor` 构造器注入（**别写字段 `@Autowired`**）。
- **Hutool / Apache Commons / Guava**：判空、字符串、集合处理优先用现成工具类（`StringUtils`、`CollectionUtils` 等），别重复造轮子。
- **日志**：用 SLF4J + Lombok（或显式 `LoggerFactory`），`com.wsf` 包默认 DEBUG 级别、彩色控制台输出；不要在业务代码用 `System.out.println`。

---

## 12. 常用扩展点（如何接新东西）

| 需求 | 在哪扩 |
|------|--------|
| 加一种登录方式（如短信/扫码） | 参考 `security/extension/mobile/`：自定义 `AuthenticationProvider` + `Token`，并在 `SecurityConfig.authenticationManager` 的 `providerList` 注册 |
| 加全局 Jackson 序列化规则 | `infrastructure/config/JacksonConfiguration.java` |
| 加接口文档分组/信息 | `config/Knife4jConfiguration.java` + `application-openapi.yml` |
| 加 Web 拦截器 | `web/WebMvcConfiguration.java`（参考 `DataSourceInterceptor`） |
| 加新的持久化通用能力 | `jpa/repository/EnhanceJpaRepository(Impl)`（所有 JPA Repo 的增强基座） |

---

## 13. 一页纸速记

```
新功能 = domain(聚合+值对象+仓储接口) → api(接口+DTO) → app(ServiceImpl 编排)
        → infrastructure(PO+Converter+仓储实现) → rest(Controller)

写操作: findById/唯一性校验 → 聚合工厂/聚合行为 → repository.save → toDto   [@Transactional]
读操作: repository.findXxx().stream().map(this::toDto).toList()
响应:   Result.success(data) / Result.failed(msg)
事件:   publisher.publishEvent(...)  /  @Async("eventUserExecutor") @EventListener
取当前用户: SecurityContextHolder.getContext().getAuthentication().getPrincipal()
```

---

*文档生成：kimi-k3*

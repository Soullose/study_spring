# study_spring 代码库框架说明（kimi-k3）

> 本文基于对仓库源码的实际阅读整理，介绍该项目的整体架构、模块划分、技术栈与关键机制。

## 1. 项目概览

`study_spring` 是一个基于 **Spring Boot 3.5.0 + Java 21** 的 Maven 多模块学习项目，采用 **DDD（领域驱动设计）分层架构**，主要覆盖以下技术点：

- **Spring Security + JWT** 登录认证与鉴权（自研 `LoginFilter`、`JwtAuthenticationTokenFilter`）
- **Spring Data JPA + QueryDSL** 持久化
- **Redis（Redisson）** 缓存与分布式能力
- **MySQL / PostgreSQL** 双数据库支持
- **Quartz** 定时任务、**MQTT（Paho v3/v5）**、**Modbus** 通信
- **Knife4j** 接口文档（启动后访问 `/doc.html`）
- **Cosid / 雪花算法 / 多种 UUID** 分布式 ID 生成
- **Resilience4j** 限流熔断、**MapStruct** 对象映射、**Lombok / Hutool / Guava** 工具库

启动入口为 `start` 模块的 `com.wsf.StartApplication`，默认端口 `40001`。

## 2. 模块结构与依赖关系

根 `pom.xml` 聚合了 8 个模块，对应 DDD 各分层：

```
start（启动模块，打包可执行 jar）
 └── 依赖以下所有模块
rest ──────────── 接口层：REST 控制器（Controller）
adapter ───────── 接口/适配器层：仅依赖 api，实现接口契约
api ──────────── 契约层：接口声明 + DTO，零实现依赖
app ──────────── 应用层：用例编排，依赖 api + domain
domain ───────── 领域层：领域模型、领域服务、仓储接口、领域事件
infrastructure ─ 基础设施层：JPA/Security/Redis/Quartz 等技术实现，依赖 domain
system ───────── 系统核心模块（老式分层：entity/service/repository/dto）
data ─────────── （空模块，预留）
```

### 各模块职责

| 模块 | DDD 分层 | 主要职责 |
|------|----------|----------|
| `domain` | 领域层 | 聚合根（`User`、`Menu`、`Role` 等）、值对象、领域服务（`UserDomainService` 等）、仓储接口（`UserRepository` 等）、领域事件（`UserCreatedEvent`、`AccountLockedEvent` 等） |
| `app` | 应用层 | 应用服务实现（`UserServiceImpl`、`AuthenticationServiceImpl`、`MenuServiceImpl` 等），编排领域对象完成用例 |
| `api` | 契约层 | 对外接口（`UserService`、`AuthenticationService` 等）与 DTO（`menu`/`user`/`role`/`datapermission`），不泄露实现 |
| `adapter` | 适配器层 | 仅依赖 `api`，实现“适配器依赖接口而非实现”的原则（目前基本为空，处于重构中） |
| `infrastructure` | 基础设施层 | 技术实现全集：JPA 实体与仓储实现、Spring Security 安全配置、多数据源、Quartz、MQTT、Modbus、VFS、ID 生成、日志 |
| `rest` | 接口层 | `LoginController`、`TestController`、`StudyController`、`EventBusDemoController` 等 |
| `system` | 系统模块 | 早期/传统的 entity-service-repository 分层代码，与新的 DDD 模块并存 |
| `start` | 启动模块 | `StartApplication` 主类、配置文件（`application*.yml`）、logback 配置 |

## 3. 典型请求链路

```
HTTP 请求
  → rest 模块 Controller（或 infrastructure 内的 AuthenticationController）
  → api 模块定义的接口（如 UserService）
  → app 模块的应用服务实现（UserServiceImpl）
  → domain 模块的领域服务 / 聚合根 / 仓储接口
  → infrastructure 模块的仓储实现（JPA + QueryDSL）
  → MySQL / PostgreSQL / Redis
```

安全链路：

```
请求 → LoginFilter（登录认证，替换 UsernamePasswordAuthenticationFilter）
     → JwtAuthenticationTokenFilter（JWT 校验）
     → UserAwareRateLimitFilter（基于 Resilience4j 的用户级限流）
     → 业务接口
```

## 4. 关键机制

### 4.1 安全认证（infrastructure/security）
- `SecurityConfig` 配置 `SecurityFilterChain`，自研三个过滤器：
  - `LoginFilter`：处理登录请求，认证成功走 `loginSuccessHandler` 颁发 JWT
  - `JwtAuthenticationTokenFilter`：解析并校验 JWT（jjwt 0.12.6）
  - `UserAwareRateLimitFilter`：感知用户身份的限流
- 配套的 `handler`、`service`、`repository`、`event`、`exception`、`extension` 子包完成认证闭环

### 4.2 持久化（infrastructure/persistence + jpa）
- JPA 实体按业务分包：`user`、`role`、`menu`、`permission`、`datapermission`、`token`
- 支持审计（`jpa/audit`）、多数据源（`datasource/annotation` + `aspect` 切面切换）
- ID 策略丰富：雪花算法、Cosid（Redis 分段）、多种 UUID 生成器

### 4.3 领域事件
- `domain/event` 定义事件基类 `BaseDomainEvent` 与 `UserCreatedEvent`、`RoleAssignedEvent`、`AccountLockedEvent` 等
- `start` 模块测试中包含 `SpringEventMigrationTest`，用于验证 Spring 事件机制迁移

### 4.4 其他基础设施
- **Quartz** 定时任务（`quartz/config`）
- **MQTT** v3/v5 双客户端、**Modbus** client/server（含测试代码）
- **Apache VFS**：`ApacheVfsApplicationContextInitializer` 在启动时初始化虚拟文件系统
- **Knife4j**：接口文档，启动日志中打印 `http://<host>:40001/doc.html`

## 5. 构建与运行

```bash
# 打包
mvn clean package

# 运行（可执行 jar 在 start/target 下）
java -jar start/target/start-1.0-SNAPSHOT.jar

# 指定 profile / 端口
java -jar start-1.0-SNAPSHOT.jar --spring.profiles.active=prod --server.port=8080
```

也可使用根目录的 `start.sh` / `start.bat` 脚本。注意：文档《启动说明.md》写 Java 17，但根 `pom.xml` 实际配置的是 **Java 21**，以 pom 为准。

## 6. 架构观察与建议

1. **领域层与 JPA 耦合**：`domain` 模块引入了 `spring-boot-starter-data-jpa`，聚合根直接暴露 JPA 注解。理想做法是领域模型保持纯净，由 `infrastructure` 的持久化实体做映射（README 中作者也已意识到这一点）。
2. **新旧两套分层并存**：`system` 模块是传统的 entity/service/repository 结构，`domain`/`app`/`api`/`adapter` 是 DDD 重构方向，目前处于过渡状态（`adapter` 基本为空，`data` 为空目录）。
3. **Controller 位置不统一**：大部分在 `rest` 模块，但认证相关的 `AuthenticationController` 位于 `infrastructure/security/auth`，建议后续归并。
4. **JaCoCo 覆盖率**：已配置，排除了 `Q*`（QueryDSL 生成类）、DTO、`system`、`adapter` 包。

---

*文档生成：kimi-k3*

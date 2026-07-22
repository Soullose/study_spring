# study_spring 认证与用户管理开发指导（kimi-k3）

> 本文面向**要收束并落地"认证与用户管理"这条最基础业务线**的开发者。
> 项目当前想法多、模块散，本文只做一件事：**从最基础的地方出发，把"用户注册、用户登录、查询用户、分页查询、权限控制、多种登录方式、JWT 无感刷新"这六块检查、校验、调整到位**，给出"现状体检 → 原理+落地方案 → 分阶段整改路线"的完整闭环。
>
> 与已有文档的关系：《study_spring-架构框架说明-kimi-k3.md》讲整体架构，《study_spring-业务开发指南-kimi-k3.md》讲通用业务怎么新增；**本文聚焦认证与用户管理这一条线的整改**，是对前两者在"安全基础"上的深化与纠偏。
>
> 文中所有"现状"结论均来自对仓库源码的实际阅读，关键处标注 `文件路径:行号`，可逐条复核。

---

## 目录

- [第一部分 · 现状体检报告](#第一部分--现状体检报告)
  - [0. 体检总览表](#0-体检总览表)
  - [1. 用户注册 —— 桩/半完成](#1-用户注册--桩半完成)
  - [2. 用户登录 —— 双链路不一致](#2-用户登录--双链路不一致)
  - [3. 查询用户 / 分页查询 —— 无 REST 出口](#3-查询用户--分页查询--无-rest-出口)
  - [4. 权限控制 —— 基本缺失](#4-权限控制--基本缺失)
  - [5. 多种登录方式 —— 空壳](#5-多种登录方式--空壳)
  - [6. JWT 与无感刷新 —— 配置硬编码、刷新缺失](#6-jwt-与无感刷新--配置硬编码刷新缺失)
- [第二部分 · 六块专题：原理 + 落地方案](#第二部分--六块专题原理--落地方案)
  - [7. 全局设计决策（先看这里）](#7-全局设计决策先看这里)
  - [8. 用户注册整改](#8-用户注册整改)
  - [9. 用户登录整改（合并双链路）](#9-用户登录整改合并双链路)
  - [10. 查询用户 / 分页查询落地](#10-查询用户--分页查询落地)
  - [11. 权限控制落地（@PreAuthorize + perms）](#11-权限控制落地preauthorize--perms)
  - [12. 多种登录方式扩展（四步法）](#12-多种登录方式扩展四步法)
  - [13. JWT 无感刷新（双 Token + Redis，重点）](#13-jwt-无感刷新双-token--redis重点)
- [第三部分 · 分阶段整改路线](#第三部分--分阶段整改路线)
  - [14. P0：先跑通（本期必做）](#14-p0先跑通本期必做)
  - [15. P1：加固](#15-p1加固)
  - [16. P2：完善](#16-p2完善)
  - [17. 附录：速查与验收清单](#17-附录速查与验收清单)

---

# 第一部分 · 现状体检报告

> 体检口径：**完整可用 / 部分实现 / 桩或空实现 / 缺失** 四档，逐项给证据与风险。

## 0. 体检总览表

| 模块 | 状态 | 一句话结论 | 关键证据 |
|------|------|-----------|----------|
| 用户注册 | 🔴 桩/半完成 | 返回假 token、无唯一性预检、邮箱不落库、DB 异常裸抛 | `AuthenticationService.java:38-53` |
| 用户登录 | 🟡 部分实现 | 功能能用，但双链路行为不一致 | `LoginFilter.java:47-79` vs `AuthenticationService.java:55-67` |
| 查询用户/分页 | 🔴 缺失 | Service 完整但无 Controller、只有 findAll 无分页、有 N+1 | `UserServiceImpl.java:111-115` |
| 权限控制 | 🔴 缺失 | 未开 `@EnableMethodSecurity`、`@PreAuthorize` 零使用、authorities 只映射角色名、账户三状态硬编码 true | `SecurityConfig.java`、`UserAccountDetail.java:36-45,59-72` |
| 多种登录方式 | 🔴 空壳 | mobile 扩展 return null、supports 恒 false、未注册；remember-me 未配置 | `MobileAuthenticationProvider.java`、`MobileUserDetailsServiceImpl.java` |
| JWT 无感刷新 | 🔴 缺失 | refresh-token 是空桩、密钥硬编码、有效期疑似笔误（约24分钟） | `AuthenticationController.java:36-39`、`JwtService.java:22,37` |

> 结论先行：**认证能"跑通登录发 token"这一步，但"注册、授权、刷新、用户管理出口"四块基本没立起来**，且存在安全硬编码。这正是要"从最基础地方检查调整"的原因。

---

## 1. 用户注册 —— 桩/半完成

**链路**：`POST /api/v1/auth/register`（白名单放行）→ `AuthenticationController.register()`（`security/auth/AuthenticationController.java:25-28`）→ `AuthenticationService.register()`（`security/service/AuthenticationService.java:38-53`）。

**已具备**：
- ✅ 密码用 **Argon2** 加密（`SecurityConfig.java:148-151`，`Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`），注册时 `passwordEncoder.encode()`（`AuthenticationService.java:41`）。

**缺口**：
1. **无用户名/邮箱唯一性预检**：`register()` 直接 `userAccountRepository.save()`，只靠 `UserAccount.username` 列的 DB unique 约束（`persistence/entity/user/UserAccount.java:31`）兜底，冲突时裸抛 `DataIntegrityViolationException`，无人捕获、无可读提示。
2. **邮箱不落库**：`RegisterRequest` 有 `email` 字段，但 `register()` 只 build 了 `UserAccount`（username/password/enabled 等），email/firstname/lastname 完全没写入；注释"简化注册逻辑，暂时不创建User实体"（`AuthenticationService.java:49`）——**User 实体压根没建**，注册出来的是个"只有账户没有人"的孤儿。
3. **返回假 token**：`RegisterResponse.token = firstname + lastname` 拼接（`AuthenticationService.java:52`），**根本不是 JWT**，前端拿到也无法用于后续鉴权。
4. **无验证码 / 邮箱验证**：全项目只有空的 `VerificationCodeException` 类，无任何验证码生成/校验逻辑。

**风险**：注册接口裸奔（可被批量撞库注册）、注册结果不可用（假 token）、数据不完整（无人无邮箱）。

---

## 2. 用户登录 —— 双链路不一致

登录功能本身能用，但**存在两条行为不一致的链路**，这是当前最大的隐性坑。

**链路 A · 过滤器链路（主）**：`LoginFilter`（拦 `POST /api/v1/auth/login`，读 JSON body 的 username/password，`LoginFilter.java:47-79`）→ `ProviderManager`（`SecurityConfig.java:137-145`）→ `DaoAuthenticationProvider` + `OpenUserDetailsService`（`SecurityConfig.java:128-134`，`hideUserNotFoundExceptions=true`）→ 成功由 `LoginSuccessHandler` 返回 JSON `{"accessToken": jwt}`（`LoginSuccessHandler.java:29-41`），失败由 `AuthenticationFailureHandlerImpl` 区分 Locked/BadCredentials 写 `ResultCode`。

**链路 B · Controller 链路（冗余）**：`POST /api/v1/auth/authenticate` → `AuthenticationService.authenticate()`（`AuthenticationService.java:55-67`），同样走 `authenticationManager`，**额外做 token 落库**（`revokeAllUserAccountTokens` 撤销旧 token + `saveUserToken` 保存新 token 到 `T_OPEN_TOKEN_`）。

**关键不一致**：
- **链路 A 不落库 token**（`LoginSuccessHandler` 只生成返回，不写 `T_OPEN_TOKEN_`、不写 Redis）；链路 B 落库。
- 而 `JwtAuthenticationTokenFilter` 校验时会查库判 revoked/expired（`JwtAuthenticationTokenFilter.java:70-71`），`LogoutHandlerImpl` 登出时置失效——**这套"撤销/登出"机制只对链路 B 签发的 token 有效**；链路 A 签发的 token 因库里无记录（`orElse(true)` 默认放行）无法被登出撤销、无法被踢人。

**已具备（可用部分）**：
- ✅ 登录失败锁定：`AuthenticationEvents` 监听 "Bad credentials" → `LoginAttemptService.recordFailedLogin()`，Redis 计数 **5 次锁 15 分钟**（`LoginAttemptService.java`），`UserAccountDetailService.loadUserDetailByUsername()` 先查锁（`UserAccountDetailService.java:30`）。

**风险**：两条链路对外都暴露在 `/api/v1/auth/**` 白名单下，调用方用哪条全凭心情；登出/踢人这一安全能力对主链路形同虚设。

---

## 3. 查询用户 / 分页查询 —— 无 REST 出口

**应用层（已具备，质量不错）**：`UserService`（api 模块接口）/ `UserServiceImpl`（app 模块）实现了 `createUser / updateUser / findById / findAll / deleteUser / createAccountForUser / unlinkAccount`，且 create/update 时有**邮箱、手机号、用户名唯一性校验**（`UserServiceImpl.java:37-48,129-135`）。

**缺口**：
1. **没有任何用户管理 Controller**：grep 确认 rest 模块无任何类引用 `UserService`，`UserController / RoleController / MenuController` 均不存在——**Service 写得再全，也没有 HTTP 出口**。
2. **无分页**：查询接口只有 `findAll()` 返回全量 `List`（`UserServiceImpl.java:111-115`），无 `Pageable / Page` 参数，数据量稍大就全量加载。
3. **N+1 风险**：`toDto()` 里每个用户都额外 `accountRepository.findByUserId()` 查一次账户（`UserServiceImpl.java:177-181`），列表查询会放大成 N+1 次 SQL。

**风险**：用户管理能力"建了地基没盖楼"，前端/管理端无接口可调；`findAll()` + N+1 在生产数据量下是性能炸弹。

---

## 4. 权限控制 —— 基本缺失

这是当前**最大的功能黑洞**：鉴权只到"登录即可访问"，没有任何细粒度授权。

**缺口**：
1. **未开方法级安全**：`SecurityConfig` 没有 `@EnableMethodSecurity`，全项目**没有任何生效的 `@PreAuthorize / hasAuthority / hasRole`**（唯一一处是 `StudyController.java:23` 的注释）。
2. **authorities 来源单薄**：`UserAccountDetail.getAuthorities()` 只把**角色名** `role.getName()` 映射为 `SimpleGrantedAuthority`（`UserAccountDetail.java:36-45`）；菜单的 `perms`（`MenuPO.java:61-62`）、按钮权限、数据权限**全部没接入**。
3. **账户状态校验失效**：`UserAccountDetail` 的 `isAccountNonExpired / isAccountNonLocked / isCredentialsNonExpired` **硬编码 `return true`**（`UserAccountDetail.java:59-72`），只有 `isEnabled` 读实体值——DB 里 `UserAccount` 的 `account_nonLocked` 等三个字段（`persistence/entity/user/UserAccount.java:38-47`）是摆设，改它对登录毫无影响。
4. **锁定逻辑双轨割裂**：实际生效的锁定是 Redis 计数锁（`LoginAttemptService`，不写库），与实体 `accountNonLocked` 字段完全脱节；`AccountLockedEvent` 只有定义、无任何发布者/监听者。
5. **数据权限 `@DataScope` 不存在**：只有领域层 `DataScope` 枚举 + `DataPermission` 实体/Service 数据模型，无注解、无 AOP、无 QueryDSL 拦截（仍是 `plans/p0_implementation_plan.md` 里的规划）。

**风险**：任何登录用户都能调任意接口，越权（水平/垂直）无防护；"账户被锁定"这件最基本的事，靠 DB 字段控制不了。

---

## 5. 多种登录方式 —— 空壳

项目预留了扩展位，但现状是**死代码**。

- **`security/extension/mobile/`（手机号登录）**：
  - `MobileAuthenticationToken` 完整；
  - 但 `MobileAuthenticationProvider.authenticate()` 验证码校验 if 体为空且 **return null**、`supports()` **恒返回 false**；
  - `MobileUserDetailsServiceImpl` 两个方法分别 `return false` / `return null`；
  - **未注册为 Bean，也未加入 `ProviderManager`**（`SecurityConfig.java:136` 处只有一行 `/// todo 添加其他provider`）。实际注册的 Provider 只有 `DaoAuthenticationProvider` 一个。
- **`security/extension/remermberme/RedisTokenRepositoryImpl`**：实现了 `PersistentTokenRepository`，但**未配置**（`LoginFilter.setRememberMeServices` 被注释，`SecurityConfig.java:123`），且构造器 `new RedisUtil()` 绕开 Spring 容器——死代码。

**风险**：看似"支持多登录方式"，实则一个都没接；照着空壳照猫画虎会踩坑。本项目 `RedisUtil` 多处 `new` 出来用、靠 `SpringUtil.getBean` 取值，本身也是个坏味道（`RedisUtil` 的 `@Component` 被注释）。

---

## 6. JWT 与无感刷新 —— 配置硬编码、刷新缺失

**已具备**：
- ✅ `JwtService`（`security/service/JwtService.java`）能正常签发/解析：claims = `iss:"w2"`、`sub:username`、`iat`、`aud:"w2-server"`、`name:username`、`jti:UUID`，HS256 签名（`:29-43`）。

**缺口**：
1. **有效期疑似笔误**：`1000 * 60 * 24` 毫秒 = **24 分钟**（`JwtService.java:37`）——若想要 24 小时应为 `* 60 * 24`，当前 token 约 24 分钟就过期。
2. **密钥硬编码在源码**：`SECRET_KEY` 是 BASE64 常量写死在 `JwtService.java:22`，无 `@Value` / 配置外置，泄漏即全网失守，且无法分环境配置。
3. **`/refresh-token` 是空桩**：`return ResponseEntity.ok("")`（`AuthenticationController.java:36-39`），**无 refresh token 生成/校验逻辑、无滑动续期**——无任何无感刷新能力。
4. **Redis 不存 token**：Redis 只用于登录失败计数（`LoginAttemptService`），不存 access/refresh token；token 只落库 `T_OPEN_TOKEN_`。
5. **并存废弃实现**：`security/utils/JwtUtil.java`（密钥 `"OPEN"`、旧 jjwt API）未使用，易误导。

**风险**：token 24 分钟过期迫使用户频繁登录，体验差；密钥硬编码是安全事故级隐患；无刷新机制，"保持长时间登录"的诉求当前完全无法满足。

---

# 第二部分 · 六块专题：原理 + 落地方案

## 7. 全局设计决策（先看这里）

在动手改任何一块之前，先统一四个贯穿全文的设计决策。它们来自对现状的体检结论，也是后续每个专题的取舍依据。

### 决策一：JWT 无感刷新用「双 Token（access + refresh）+ Redis」

- **access token**：短效（建议 30 分钟），无状态，每次请求由 `JwtAuthenticationTokenFilter` 校验。
- **refresh token**：长效（建议 7 天），**存 Redis**，key 带用户标识，可主动撤销（登出/踢人/改密后立即失效）。
- **为什么不用滑动续期（单 token 自动续签）**：滑动续期无法主动撤销、旧 token 无法作废，一旦泄露窗口期不可控；双 Token 把"能不能换新"的判断收敛到 Redis，服务端握有绝对控制权，最适合"保持长时间登录 + 可管可控"的诉求。
- 现有 `T_OPEN_TOKEN_` 表（`persistence/entity/token/Token.java`）保留用于**登录审计**，但 refresh token 的有效性判断以 **Redis 为准**（DB 只做记录，不做实时校验，避免每请求查库）。

### 决策二：注册 / 登录收敛为「单链路」

现状有两条登录链路（详见第 2 节体检），行为不一致。决策：**保留过滤器链路（`LoginFilter`）作为唯一登录入口**，把"撤销旧 token + 落库 + 写 Redis refresh token"的逻辑下沉到 `LoginSuccessHandler`；废弃 `AuthenticationController.authenticate()` 这个冗余第二入口（或改为内部复用同一套服务）。注册入口保留在 `/api/v1/auth/register`，但逻辑重写（见第 8 节）。

### 决策三：权限模型用「角色 + 菜单 perms」接入 `@PreAuthorize`

- 开启 `@EnableMethodSecurity`（当前 `SecurityConfig` 未开）。
- `UserAccountDetail.getAuthorities()` 当前只映射角色名（`UserAccountDetail.java:36-45`），要扩展为：**角色名（`ROLE_xxx`）+ 角色关联菜单的 `perms` 字符串**，两者都装进 `SimpleGrantedAuthority`。
- 接口侧用 `@PreAuthorize("hasAuthority('user:list')")` 做方法级鉴权，按钮/菜单的 `perms_` 字段（`MenuPO.java:61-62`）就是权限标识来源。

### 决策四：分页统一用 Spring `Pageable` + QueryDSL

- api 层分页接口签名统一为 `Page<UserDto> page(UserQueryRequest query)`，内部用 `PageRequest.of(pageNum, pageSize)`。
- infrastructure 层用 `JPAQueryFactory` 做动态条件 + 分页（`offset/limit` + `fetchCount`），避免 `findAll()` 全量加载和 N+1。

> 这四个决策会在第二部分逐个落地为具体代码骨架。

### 目标状态的安全链路图

整改完成后，一次请求的完整链路与双 Token 的流转如下。

**请求鉴权链路（过滤器顺序）**：

```
HTTP 请求
  → LoginFilter            （仅拦 POST /api/v1/auth/login，走账号密码认证）
  → JwtAuthenticationTokenFilter  （解析 Bearer access token，校验签名+有效期）
  → UserAwareRateLimitFilter      （感知用户身份的限流）
  → DispatcherServlet → Controller（@PreAuthorize 在此做方法级鉴权）
```

**双 Token 流转（无感刷新）**：

```
登录 POST /api/v1/auth/login
  │ 认证成功（LoginSuccessHandler）
  ├─ 生成 accessToken（30min，无状态）
  ├─ 生成 refreshToken（7d，jti 唯一）
  ├─ refreshToken 写 Redis： key=login:refresh:{userId}  value={jti}  TTL=7d
  ├─ 撤销旧 token + 本次登录落库 T_OPEN_TOKEN_（审计）
  └─ 返回 { accessToken, refreshToken }

业务请求（带 accessToken）
  │ JwtAuthenticationTokenFilter 校验通过 → 放行

accessToken 过期（前端收到 401 / 特定 code）
  │ 前端静默调 POST /api/v1/auth/refresh-token  （带 refreshToken）
  ├─ 校验 refreshToken 签名 + 有效期
  ├─ 取 jti，比对 Redis login:refresh:{userId} 是否一致（不一致=已撤销/被踢）
  ├─ 一致 → 轮换：签发新 accessToken + 新 refreshToken，更新 Redis 的 jti
  └─ 返回新 { accessToken, refreshToken }，前端无感续上

登出 / 改密 / 管理员踢人
  └─ 删 Redis login:refresh:{userId} + 撤销 DB token 记录 → refreshToken 立即失效，
     accessToken 最多再存活一个短周期（30min）后自然过期
```

**权限加载点**：登录或 refresh 时 `UserAccountDetailService.loadUserDetailByUsername()` 查角色（`UserAccountDetailService.java:38-43`），整改后在此一并加载角色关联菜单的 `perms`，装进 `UserAccountDetail.getAuthorities()`，供 `@PreAuthorize` 在 Controller 方法上判定。

---

## 8. 用户注册整改

**目标**：注册即建"人 + 账户"，做唯一性预检，返回可用凭证（或明确的"待激活"状态），异常可读。

**改哪里**：`security/service/AuthenticationService.java` 的 `register()`。

**要点**：
1. **唯一性预检**：save 前查 `userAccountRepository.findByUsername()`，已存在则抛业务异常（配合第 15 节的全局异常处理返回友好提示），不要靠 DB 约束裸抛。
2. **补齐 User 实体**：用 `RegisterRequest` 的 `firstname/lastname/email` 建 `persistence/entity/user/User`，并和 `UserAccount` 关联（`User.userAccount` 是 `@OneToOne`，`User.java:56`）。
3. **账户状态初始化**：`enabled=true`，`accountNonExpired/accountNonLocked/credentialsNonExpired` 按"未过期/未锁定"语义给 `true`（注意：当前注册给了 `false`，配合 `UserAccountDetail` 硬编码 true 才没出事——整改见第 11 节）。
4. **返回值**：注册成功后直接签发双 token（与登录一致），或返回"注册成功请登录"。不要返回 `firstname+lastname` 这种假 token。

**代码骨架（`AuthenticationService.register()` 重写）**：

```java
@Transactional
public RegisterResponse register(RegisterRequest request) {
    // 1. 唯一性预检
    if (userAccountRepository.findByUsername(request.getUsername()).isPresent()) {
        throw new IllegalArgumentException("用户名已被占用: " + request.getUsername());
    }
    // 2. 建账户（状态字段按"正常"初始化）
    UserAccount account = UserAccount.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .enabled(true)
            .accountNonExpired(true)      // true = 未过期
            .accountNonLocked(true)       // true = 未锁定
            .credentialsNonExpired(true)  // true = 凭证未过期
            .build();
    // 3. 建 User 并关联账户（人 + 账户一体）
    User user = new User();
    user.setFirstname(request.getFirstname());
    user.setLastname(request.getLastname());
    user.setEmail(request.getEmail());
    user.setUserAccount(account);
    userAccountRepository.save(account);   // 级联或分别 save，视映射配置
    // 4. 签发双 token（复用第 13 节的 TokenIssueService）
    TokenPair pair = tokenIssueService.issue(account);
    return RegisterResponse.builder()
            .token(pair.accessToken())
            .refreshToken(pair.refreshToken())
            .build();
}
```

**DTO 调整**：`RegisterResponse` 增加 `refreshToken` 字段；`RegisterRequest` 加 `@NotBlank` 等校验注解，Controller 加 `@Validated`。

**验收**：`curl -X POST /api/v1/auth/register -H 'Content-Type: application/json' -d '{"username":"u1","password":"p@123","firstname":"张","lastname":"三","email":"u1@x.com"}'` 返回真 accessToken+refreshToken；重复注册同 username 返回"用户名已被占用"而非 500 堆栈。

---

## 9. 用户登录整改（合并双链路）

**目标**：只保留一条登录链路，让"撤销旧 token / 落库 / 写 Redis refresh token"对**所有**登录生效。

**改哪里**：
1. **下沉逻辑到 `LoginSuccessHandler`**（过滤器链路的成功回调）：把 `AuthenticationService` 里的 `revokeAllUserAccountTokens()` + `saveUserToken()` + 第 13 节的"签发双 token + 写 Redis"逻辑，抽成一个公共的 `TokenIssueService.issue(UserAccount)`，`LoginSuccessHandler` 调它。
2. **收敛第二入口**：`AuthenticationController.authenticate()` 改为也调 `TokenIssueService.issue()`（或标注 `@Deprecated` 引导前端统一走 `/login`）。两个入口产出一致，消除"链路 A 无法登出"的坑。

**代码骨架（`TokenIssueService`，登录/注册/refresh 三处复用）**：

```java
@Service
@RequiredArgsConstructor
public class TokenIssueService {
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final RedisTokenStore redisTokenStore;   // 见第 13 节

    public TokenPair issue(UserAccount account) {
        UserAccountDetail ud = new UserAccountDetail(account);
        String accessToken  = jwtService.generateAccessToken(ud);
        String refreshToken = jwtService.generateRefreshToken(ud);   // 含 jti
        // 撤销历史 token（DB 审计维度）
        revokeAllUserAccountTokens(account);
        saveUserToken(account, accessToken);
        // refresh token 写 Redis（有效性以 Redis 为准）
        redisTokenStore.saveRefreshToken(account.getId(), jwtService.extractJti(refreshToken));
        return new TokenPair(accessToken, refreshToken);
    }
    // revokeAllUserAccountTokens / saveUserToken 从 AuthenticationService 平移过来
}

public record TokenPair(String accessToken, String refreshToken) {}
```

**`LoginSuccessHandler` 改造**：

```java
public void onAuthenticationSuccess(...) {
    UserAccountDetail ud = (UserAccountDetail) authentication.getPrincipal();
    TokenPair pair = tokenIssueService.issue(ud.getUserAccount());
    ResponseUtils.writeSuccessMsg(response, Map.of(
            "accessToken", pair.accessToken(),
            "refreshToken", pair.refreshToken()));
}
```

**验收**：分别走 `POST /api/v1/auth/login` 和 `/authenticate`，两者都返回 accessToken+refreshToken 且都在 `T_OPEN_TOKEN_` 落库、Redis 有 `login:refresh:{userId}`；登出后旧 refreshToken 立即失效。

---

## 10. 查询用户 / 分页查询落地

**目标**：给 `UserService` 补上 HTTP 出口和分页能力，消除 N+1。

**改动四步（沿 DDD 分层自内向外）**：

**① domain 层**：`UserRepository` 接口加分页方法（返回领域层自己的分页结果，不依赖 Spring `Page`，保持 domain 纯净）：

```java
// domain/repository/UserRepository.java 增加
PageResult<User> pageQuery(UserQuery query);   // PageResult/UserQuery 是 domain 自定义的简单分页载体
```

> 简化做法：若不想在 domain 自定义分页载体，可让仓储直接返回 `List<User>` + `long total`（用一个 `record` 或 `long` 返回值分开查），由 app 层组装 Spring `Page`。

**② infrastructure 层**：`UserRepositoryImpl` 用 `JPAQueryFactory` 实现动态条件 + 分页：

```java
private final JPAQueryFactory queryFactory;   // 已有 Bean，直接注入

@Override
public PageResult<User> pageQuery(UserQuery q) {
    QUser po = QUser.user;
    BooleanBuilder where = new BooleanBuilder();
    if (StringUtils.hasText(q.getEmail()))       where.and(po.email.likeIgnoreCase("%" + q.getEmail() + "%"));
    if (StringUtils.hasText(q.getPhoneNumber())) where.and(po.phoneNumber.like("%" + q.getPhoneNumber() + "%"));
    if (StringUtils.hasText(q.getRealName()))    where.and(po.realName.like("%" + q.getRealName() + "%"));

    List<User> content = queryFactory.selectFrom(po).where(where)
            .offset(q.pageable().getOffset()).limit(q.pageable().getPageSize())
            .orderBy(po.createTime.desc()).fetch()
            .stream().map(converter::toDomain).toList();
    long total = queryFactory.select(po.count()).from(po).where(where).fetchOne();
    return new PageResult<>(content, total, q.pageable());
}
```

**③ app 层**：`UserServiceImpl` 把 domain 分页结果组装成 `Page<UserDto>`，并**一次性批量查账户**消除 N+1：

```java
public Page<UserDto> page(UserQueryRequest req) {
    PageRequest pr = PageRequest.of(req.getPageNum(), req.getPageSize());
    PageResult<User> result = userRepository.pageQuery(new UserQuery(req.getEmail(), req.getPhoneNumber(), req.getRealName(), pr));
    // 批量取所有用户 id 的账户，map 起来，避免逐条 findByUserId
    Map<String, UserAccount> accountMap = accountRepository.findByUserIdIn(
            result.content().stream().map(User::getId).toList())
        .stream().collect(toMap(UserAccount::getUserId, a -> a));
    List<UserDto> dtoList = result.content().stream()
            .map(u -> toDto(u, accountMap.get(u.getId()))).toList();
    return new PageImpl<>(dtoList, pr, result.total());
}
```

> 配套：`toDto` 重载为接收可选的 `UserAccount`，不再内部查库；`UserAccountRepository` 增加 `findByUserIdIn(Collection<String>)`。

**④ rest 层**：新增 `UserController`（rest 模块，只依赖 api 接口）：

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")            // 配合第 11 节
    public Result<Page<UserDto>> page(@Validated UserQueryRequest req) {
        return Result.success(userService.page(req));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:query')")
    public Result<UserDto> get(@PathVariable String id) {
        return userService.findById(id).map(Result::success).orElse(Result.failed("用户不存在"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public Result<UserDto> create(@Validated @RequestBody CreateUserRequest req) {
        return Result.success(userService.createUser(req));
    }
    // update / delete 同理
}
```

**api 层新增 `UserQueryRequest`**：`pageNum`（默认 0）、`pageSize`（默认 10，上限 100 防恶意大分页）、`email/phoneNumber/realName`（可选查询条件）。

**验收**：`curl '/api/users?pageNum=0&pageSize=10&email=wang' -H "Authorization: Bearer <token>"` 返回分页结构 `{content,totalElements,totalPages,...}`；观察日志确认账户查询是 1 次 `IN` 而非 N 次单查。

---

## 11. 权限控制落地（@PreAuthorize + perms）

**目标**：从"登录即可"升级为"按权限标识鉴权"，并修活账户状态字段。

**改动五步**：

**① 开启方法级安全**（`SecurityConfig` 类上加注解）：

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // 新增：开启 @PreAuthorize/@PostAuthorize 等
public class SecurityConfig { ... }
```

**② 扩展 authorities 来源**（`UserAccountDetail.getAuthorities()`）：角色名 + 角色关联菜单的 `perms` 都装入：

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<Role> roles = userAccount.getRoles();
    if (roles == null || roles.isEmpty()) return Collections.emptyList();
    Set<GrantedAuthority> auths = new HashSet<>();
    for (Role role : roles) {
        auths.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));   // 角色
        if (role.getMenus() != null) {
            role.getMenus().stream()
                 .map(Menu::getPerms).filter(StringUtils::hasText)
                 .forEach(p -> auths.add(new SimpleGrantedAuthority(p)));  // 菜单/按钮权限标识
        }
    }
    return auths;
}
```

> 配套：`UserAccountDetailService.loadUserDetailByUsername()` 查角色时要**连带查出角色的 menus**（`findByUserAccounts` 后 `role.getMenus()` 别 Lazy 失败，可用 `fetch join` 或 `@EntityGraph`）。

**③ 修活账户状态字段**（`UserAccountDetail.java:59-72`，把硬编码 true 改回读实体）：

```java
@Override public boolean isAccountNonExpired()     { return userAccount.isAccountNonExpired(); }
@Override public boolean isAccountNonLocked()      { return userAccount.isAccountNonLocked(); }
@Override public boolean isCredentialsNonExpired() { return userAccount.isCredentialsNonExpired(); }
@Override public boolean isEnabled()               { return userAccount.isEnabled(); }
```

> ⚠️ 配套影响：DB 里存量数据若这三字段是 `false`（当前注册就给了 false），改回读实体后会登录被拒。需先刷库：`UPDATE T_OPEN_USER_ACCOUNT_ SET account_nonExpired=1, account_nonLocked=1, credentials_nonExpired=1;`，并让注册/创建账户正确初始化（见第 8 节骨架）。

**④ 接口侧加鉴权**：在 `UserController` 等方法上加 `@PreAuthorize("hasAuthority('user:list')")`（见第 10 节骨架）；权限标识与菜单 `perms_` 字段值一一对应。

**⑤ 权限标识规范**：统一 `{资源}:{动作}` 格式，如 `user:list / user:query / user:create / user:update / user:delete / role:assign`。在菜单管理里维护这些 `perms`，分配给角色。

**验收**：用无 `user:list` 权限的账号调 `GET /api/users` 返回 403（`AccessDeniedHandlerImpl`）；给角色配上 `user:list` 菜单后同账号可调通；把账号 `enabled` 置 0 后登录被拒。

---

## 12. 多种登录方式扩展（四步法）

**目标**：掌握"加一种登录方式"的标准套路，并能判断现有 mobile 死代码是救活还是删除。

**原理**：Spring Security 的认证是 `AuthenticationManager`（`ProviderManager`）遍历一组 `AuthenticationProvider`，每个 Provider 用 `supports()` 声明"我认哪种 `AuthenticationToken`"，认上了就 `authenticate()`。加登录方式 = 提供"自定义 Token + Provider + UserDetailsService + 注册进 ProviderManager"四件套。

**四步法**：

```
① 自定义 Token     extends AbstractAuthenticationToken（如 MobileAuthenticationToken）
                   未认证时 authenticated=false 只存手机号；认证成功后存 UserDetails + authorities
② 自定义 Provider  implements AuthenticationProvider
                   supports()  → return MobileAuthenticationToken.class.isAssignableFrom(authentication);
                   authenticate() → 校验验证码 → loadUserByMobile → 构造已认证 Token 返回（不能 return null）
③ UserDetailsService  按新凭证维度查用户（如按手机号），返回 UserAccountDetail
④ 注册进 ProviderManager  在 SecurityConfig.authenticationManager 的 providerList.add(mobileProvider)
```

**救活现有 mobile 死代码的关键修复点**（当前 `MobileAuthenticationProvider.java`）：

```java
// 修复 1：supports() 恒 false → 改为识别 mobile token
@Override
public boolean supports(Class<?> authentication) {
    return MobileAuthenticationToken.class.isAssignableFrom(authentication);   // 原来 return false
}

// 修复 2：authenticate() return null → 补全校验并返回已认证 token
@Override
public Authentication authenticate(Authentication authentication) {
    String mobile = (String) authentication.getPrincipal();
    String code   = (String) authentication.getCredentials();
    // a. 校验短信验证码（对 Redis 里 login:sms:{mobile} 的值）
    if (!smsCodeService.verify(mobile, code)) throw new BadCredentialsException("验证码错误或已过期");
    // b. 按手机号查用户
    UserAccountDetail ud = mobileUserDetailsService.loadUserByMobile(mobile);   // 原来 return null
    // c. 构造已认证 token（带 authorities）
    return MobileAuthenticationToken.authenticated(ud, ud.getAuthorities());
}
```

**注册**（`SecurityConfig.java:136` 的 `/// todo 添加其他provider` 处）：

```java
@Bean
AuthenticationManager authenticationManager(DaoAuthenticationProvider dao,
                                            MobileAuthenticationProvider mobile) {
    List<AuthenticationProvider> list = new ArrayList<>();
    list.add(dao);
    list.add(mobile);        // 注册 mobile provider
    ProviderManager pm = new ProviderManager(list);
    pm.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(applicationEventPublisher));
    return pm;
}
```

**入口**：移动端可用一个 `MobileLoginFilter`（仿 `LoginFilter`，拦 `POST /api/v1/auth/login/mobile`，读 `mobile/code` 构造 `MobileAuthenticationToken`），成功仍走 `LoginSuccessHandler` → 复用第 9 节 `TokenIssueService` 签发双 token。

**决策建议**：
- **本期若无手机号登录需求** → 直接**删除** `extension/mobile` 与 `extension/remermberme` 死代码，避免误导（remember-me 的 `RedisTokenRepositoryImpl` 未配置且 `new RedisUtil()` 绕开 Spring）。
- **本期要做** → 按上面修复点救活 mobile，并补 `smsCodeService`（验证码生成/校验，存 Redis `login:sms:{mobile}`，5 分钟有效、防重发）。

**验收**：`POST /api/v1/auth/login/mobile` 带正确 `mobile+code` 返回双 token；错误验证码返回 401；`ProviderManager` 日志可见 mobile provider 被命中。

---

## 13. JWT 无感刷新（双 Token + Redis，重点）

这是本次整改的**核心**，对应"保持长时间登录 + 无感刷新"的诉求。方案已在第 7 节定为「双 Token + Redis」，本节给出完整落地。

### 13.1 原理回顾

- access token 短效、无状态、每次请求校验；过期后**不重新登录**，用 refresh token 静默换新的。
- refresh token 长效、**存 Redis**，服务端可主动撤销（登出/踢人/改密）。`jti`（JWT ID）与 Redis 中存的值比对，是"这把 refresh token 还有效吗"的判据；**每次刷新都轮换**（refresh token rotation），旧 refresh token 立即作废，防重放。

### 13.2 Redis key 设计

| key | value | TTL | 说明 |
|-----|-------|-----|------|
| `login:refresh:{userId}` | refresh token 的 `jti` | 7 天（与 refresh 有效期一致） | 刷新时比对 jti；登出/踢人删除此 key |
| `login:sms:{mobile}` | 短信验证码 | 5 分钟 | 仅 mobile 登录用（见第 12 节） |
| `failed-attempts:{username}` / `account-lock:{username}` | 失败计数 / 锁标记 | 15 分钟 | 已有（`LoginAttemptService`），保持 |

> 用 Redisson：`redissonClient.getBucket(key).get() / .set(jti, Duration.ofDays(7)) / .delete()`。

### 13.3 JwtService 重构

**改哪里**：`security/service/JwtService.java`。

**要点**：
1. **密钥外置**：删掉硬编码 `SECRET_KEY`（`:22`），改为从配置读 `@Value("${security.jwt.secret}")`，配置放 `application-security.yml`（并在 `application.yml` 的 `include` 加 `security`）。生产密钥用环境变量注入，不进仓库。
2. **有效期修正**：`:37` 的 `1000*60*24`（24 分钟）是笔误。access/refresh 有效期都外置：
   ```yaml
   security:
     jwt:
       secret: ${JWT_SECRET:请换成足够长的BASE64密钥}
       access-token-ttl: PT30M      # 30 分钟
       refresh-token-ttl: P7D       # 7 天
   ```
3. **拆分生成方法**：`generateAccessToken()`（sub=username，含权限/角色 claims）与 `generateRefreshToken()`（只含 sub + jti，不携带权限），并加 `extractJti(token)`。
4. **删除废弃** `security/utils/JwtUtil.java`，避免误导。

**代码骨架**：

```java
@Service
public class JwtService {
    @Value("${security.jwt.secret}")        private String secret;
    @Value("${security.jwt.access-token-ttl}")  private Duration accessTtl;
    @Value("${security.jwt.refresh-token-ttl}") private Duration refreshTtl;

    public String generateAccessToken(UserDetails ud) {
        return Jwts.builder().header().type("JWT").and()
                .issuer("w2").subject(ud.getUsername())
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + accessTtl.toMillis()))
                .audience().add("w2-server").and()
                .id(UUID.randomUUID().toString())
                .signWith(key(), Jwts.SIG.HS256).compact();
    }

    public String generateRefreshToken(UserDetails ud) {
        return Jwts.builder().subject(ud.getUsername())
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + refreshTtl.toMillis()))
                .id(UUID.randomUUID().toString())            // jti，Redis 比对的凭据
                .signWith(key(), Jwts.SIG.HS256).compact();
    }

    public String extractJti(String token)  { return extractClaim(token, Claims::getId); }
    public String extractUsername(String t) { return extractClaim(t, Claims::getSubject); }
    private SecretKey key() { return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)); }
    // extractClaim / isTokenValid 沿用现有实现
}
```

### 13.4 RedisTokenStore（refresh token 存取）

```java
@Component
@RequiredArgsConstructor
public class RedisTokenStore {
    private final RedissonClient redissonClient;
    private static final String KEY = "login:refresh:";

    public void saveRefreshToken(String userId, String jti) {
        redissonClient.getBucket(KEY + userId).set(jti, Duration.ofDays(7));
    }
    public boolean isValid(String userId, String jti) {
        Object v = redissonClient.getBucket(KEY + userId).get();
        return v != null && v.toString().equals(jti);
    }
    public void revoke(String userId) { redissonClient.getBucket(KEY + userId).delete(); }
}
```

### 13.5 refresh 端点改造（替换空桩）

`AuthenticationController.refreshToken()`（当前 `:36-39` 返回 `ok("")`）改为：

```java
@PostMapping("/refresh-token")
public Result<TokenPair> refreshToken(@RequestBody @Validated RefreshRequest req) {
    return Result.success(authenticationService.refresh(req.getRefreshToken()));
}
```

```java
// AuthenticationService 新增
public TokenPair refresh(String refreshToken) {
    String username = jwtService.extractUsername(refreshToken);   // 签名+过期由 jjwt 校验，过期抛 ExpiredJwtException
    String jti      = jwtService.extractJti(refreshToken);
    UserAccountDetail ud = userAccountDetailService.loadUserDetailByUsername(username);
    String userId = ud.getUserAccount().getId();
    // 比对 Redis：不一致 = 已撤销/被踢/旧 token 重放
    if (!redisTokenStore.isValid(userId, jti)) {
        throw new IllegalArgumentException("refresh token 已失效，请重新登录");
    }
    // 轮换：签发新双 token（issue 内部会更新 Redis 的 jti，旧 refresh 即刻作废）
    return tokenIssueService.issue(ud.getUserAccount());
}
```

> 注意 `/api/v1/auth/**` 已在白名单，refresh 端点无需 access token，凭 refresh token 本身即可调。

### 13.6 登出 / 踢人

`LogoutHandlerImpl` 在置 DB token 失效的基础上，**补一行** `redisTokenStore.revoke(userId)` 删 Redis key → refresh token 立即失效；access token 最多再活一个 30 分钟短周期。管理员"踢人"同理调 `redisTokenStore.revoke(userId)`。

### 13.7 JwtAuthenticationTokenFilter 微调

保持校验 access token 签名+有效期即可；由于有效性已主要由"签名+短有效期+refresh 可控"保证，**DB 逐请求查 revoked 这步可保留（强一致）或去掉（性能）**——本项目 token 量不大，建议保留以支持"改密后立即踢掉 access token"。另注意 `:108` 在 catch 后仍 `filterChain.doFilter`，要确保异常分支不再重复放行（已有 `ResponseUtils.writeErrMsg` 的分支要 `return`）。

**验收**：
1. 登录拿到 access(30min)+refresh(7d)；`redis-cli KEYS "login:refresh:*"` 有记录。
2. 带 access 调业务接口通；**人为把 access-token-ttl 调成 1 分钟**，等过期后用 refresh 调 `/refresh-token` 拿到新双 token，业务接口恢复可用（全程无重新登录 = 无感）。
3. 旧 refresh token 再次调 `/refresh-token` 被拒（已轮换作废）。
4. 登出后 refresh token 调 `/refresh-token` 返回"已失效"。

---

# 第三部分 · 分阶段整改路线

> 按 **P0 先跑通 → P1 加固 → P2 完善** 推进。每阶段给出涉及文件清单、验证方式、回滚注意点。P0 是本期"把握基础"的最小闭环。

## 14. P0：先跑通（本期必做）

目标：注册/登录/刷新/用户查询/基础权限这条主线打通且行为一致。

| # | 事项 | 涉及文件 | 对应章节 |
|---|------|----------|----------|
| 1 | 注册补唯一性预检 + 邮箱落库 + 返回真双 token + 账户状态正确初始化 | `AuthenticationService.register()`、`RegisterResponse` | 第 8 节 |
| 2 | 登录合并单链路：抽 `TokenIssueService`，`LoginSuccessHandler` 下沉签发，收敛 `authenticate()` 第二入口 | `TokenIssueService`(新)、`LoginSuccessHandler`、`AuthenticationController` | 第 9 节 |
| 3 | JWT 密钥外置 + 有效期修正（24min→30min）+ 拆 access/refresh | `JwtService`、`application-security.yml`(新)、`application.yml`(include) | 第 13.3 节 |
| 4 | 双 Token + Redis 刷新落地：`RedisTokenStore` + refresh 端点替换空桩 + 登出 revoke | `RedisTokenStore`(新)、`AuthenticationService.refresh()`、`AuthenticationController`、`LogoutHandlerImpl` | 第 13.4-13.6 节 |
| 5 | 开启 `@EnableMethodSecurity` + `getAuthorities()` 接入 perms + 修活账户三状态（先刷库） | `SecurityConfig`、`UserAccountDetail`、`UserAccountDetailService` | 第 11 节 |
| 6 | 补 `UserController` 分页查询（api 加 `page` + `UserQueryRequest`、infra QueryDSL 分页、消 N+1） | `UserController`(新)、`UserService`、`UserServiceImpl`、`UserRepository(Impl)` | 第 10 节 |

**验证方式**（按顺序跑一遍即覆盖主线）：

```bash
# 1. 注册
curl -X POST localhost:40001/api/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"u1","password":"P@ssw0rd","firstname":"张","lastname":"三","email":"u1@x.com"}'
# 2. 登录（拿到 accessToken + refreshToken）
curl -X POST localhost:40001/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"u1","password":"P@ssw0rd"}'
# 3. 分页查询（带 accessToken，需先有 user:list 权限）
curl "localhost:40001/api/users?pageNum=0&pageSize=10" -H "Authorization: Bearer <accessToken>"
# 4. 刷新（把 access-token-ttl 临时调 1min 等过期后）
curl -X POST localhost:40001/api/v1/auth/refresh-token -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refreshToken>"}'
# 5. 登出后再刷新应失效
curl -X POST localhost:40001/api/v1/auth/logout -H "Authorization: Bearer <accessToken>"
```

**回滚注意点**：
- 第 11 节"修活账户三状态"依赖**先刷库**（`UPDATE T_OPEN_USER_ACCOUNT_ SET account_nonExpired=1, account_nonLocked=1, credentials_nonExpired=1;`），否则存量用户登录被拒；此项改动可单独开关，出问题先回退 `UserAccountDetail` 的三个方法再补数据。
- 密钥外置后，旧密钥签发的存量 token 会全部失效——上线即视为"全员重新登录"，属预期。
- `authenticate()` 第二入口若前端在用，先 `@Deprecated` 观察一段时间再删，别一刀切。

---

## 15. P1：加固

| # | 事项 | 说明 | 对应章节 |
|---|------|------|----------|
| 1 | 全局异常处理 `@RestControllerAdvice` | 把 `IllegalArgumentException`、唯一性冲突、参数校验异常（`MethodArgumentNotValidException`）统一映射为 `Result{code,msg}`，不再裸抛 500 堆栈 | 关联整改（P0 的注册/登录异常依赖它） |
| 2 | 登录失败锁定与账户状态字段打通 | `LoginAttemptService` 锁定（Redis）达到上限时，回写 `UserAccount.accountNonLocked=false`；解锁时恢复——消除"Redis 锁与 DB 字段双轨割裂" | 第 11 节 |
| 3 | 注册/登录验证码 | 图形或滑块验证码防批量撞库注册、防爆破；存 Redis 短 TTL | 第 8 节扩展点 |
| 4 | Controller 统一 `Result` 包装 | 现有 `AuthenticationController` 直接返回 DTO，统一为 `Result<T>`；`AccessDeniedHandlerImpl` 返回标准 403 Result 而非明文 | 第 10、11 节 |

**验证**：注册重复 username 返回 `{code:xxx,msg:"用户名已被占用"}` 而非堆栈；连续 5 次错误密码后第 6 次提示"账户被锁定"且 DB `account_nonLocked=0`；无验证码调注册被拒。

---

## 16. P2：完善

| # | 事项 | 说明 | 对应章节 |
|---|------|------|----------|
| 1 | mobile 登录补全或删除 | 按第 12 节决策：要么补 `smsCodeService` + 修 provider 上线，要么删死代码 | 第 12 节 |
| 2 | remember-me 接入 Redis 或移除 | `RedisTokenRepositoryImpl` 当前未配置且 `new RedisUtil()` 绕开 Spring；要么正确接入，要么删除 | 第 12 节 |
| 3 | 数据权限 `@DataScope` | 注解 + AOP + QueryDSL 拦截，按 `DataPermission.dataScope` 过滤行级数据（详见 `plans/p0_implementation_plan.md` 的 P0-4） | 关联规划 |
| 4 | 权限缓存 + 变更广播 | `getAuthorities()` 每请求查角色+菜单有开销，用 Redisson 缓存 + 发布/订阅在角色/菜单变更时失效 | 关联规划 |

---

## 17. 附录：速查与验收清单

### 17.1 关键端点速查（整改后）

| 端点 | 方法 | 说明 | 是否需登录 |
|------|------|------|-----------|
| `/api/v1/auth/register` | POST | 注册（人+账户），返回双 token | 否（白名单） |
| `/api/v1/auth/login` | POST | 账号密码登录（`LoginFilter`），返回双 token | 否（白名单） |
| `/api/v1/auth/refresh-token` | POST | 用 refreshToken 换新双 token（轮换） | 否（白名单，凭 refresh token） |
| `/api/v1/auth/logout` | POST | 登出，撤 DB token + 删 Redis refresh | 是 |
| `/api/users` | GET | 分页查询用户，需 `user:list` 权限 | 是 |
| `/api/users/{id}` | GET | 查单个用户，需 `user:query` 权限 | 是 |
| `/api/users` | POST | 创建用户，需 `user:create` 权限 | 是 |

### 17.2 Redis key 速查

| key | 用途 | TTL |
|-----|------|-----|
| `login:refresh:{userId}` | refresh token 的 jti，刷新比对/登出删除 | 7 天 |
| `login:sms:{mobile}` | 短信验证码（mobile 登录） | 5 分钟 |
| `failed-attempts:{username}` | 登录失败计数 | 15 分钟 |
| `account-lock:{username}` | 账户锁标记 | 15 分钟 |

### 17.3 总验收清单（DoD）

- [ ] 注册：唯一性冲突返回可读提示；邮箱落库；返回真双 token
- [ ] 登录：`/login` 与 `/authenticate` 产出一致（双 token + 落库 + Redis）
- [ ] 刷新：access 过期后凭 refresh 静默换新；旧 refresh 重放被拒；登出后 refresh 失效
- [ ] 分页：`GET /api/users` 返回 `Page` 结构；账户查询为 1 次 IN（无 N+1）
- [ ] 权限：无 `user:list` 调列表返回 403；配置权限后调通；`enabled=0` 登录被拒
- [ ] JWT：密钥在配置而非源码；access 30min/refresh 7d；无废弃 `JwtUtil`
- [ ] 账户状态：`accountNonLocked` 等三字段读实体值，改库即生效

---

> 整改顺序建议：先做第 13 节（JWT 重构 + 双 token）打地基 → 第 9 节（登录合并）→ 第 8 节（注册）→ 第 11 节（权限）→ 第 10 节（用户查询出口）→ 第 12 节（多登录，按需）。每完成一块就用对应"验收"小节的 curl 验证，再进下一块。

---

*文档生成：kimi-k3*







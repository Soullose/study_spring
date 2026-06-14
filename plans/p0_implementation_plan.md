# P0 核心功能缺失项 —— 详细技术实施方案

> 优先级：🔴 P0（必须立即补齐） | 共 6 项
> 基于规范：DDD 分层架构 / Spring Boot 3.5.0 / JDK 21 / Redisson 3.x / QueryDSL 5.x / Bouncy Castle 1.78

---

## 目录

1. [P0-1: 编码规则引擎](#p0-1-编码规则引擎)
2. [P0-2: Bouncy Castle 加密模块](#p0-2-bouncy-castle-加密模块)
3. [P0-3: REST API 控制器层](#p0-3-rest-api-控制器层)
4. [P0-4: @DataScope 注解 + QueryDSL 拦截器](#p0-4-datascope-注解--querydsl-拦截器)
5. [P0-5: 按钮权限模型 + @PreAuthorize](#p0-5-按钮权限模型--preauthorize)
6. [P0-6: 权限缓存 + Redisson 发布/订阅](#p0-6-权限缓存--redisson-发布订阅)
7. [整体架构调整建议](#整体架构调整建议)
8. [全局技术风险清单](#全局技术风险清单)

---

## P0-1: 编码规则引擎

### 1.1 领域边界划分

```
┌─────────────────────────────────────────────────────┐
│  Bounded Context: Serial Number (编码管理)            │
│                                                       │
│  ┌──────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │ RuleDef   │  │ Segments     │  │ SequenceGen   │  │
│  │ (ruleCode)│  │ [FIXED|DATE │  │ (RAtomicLong) │  │
│  │           │  │  |SEQUENCE]  │  │               │  │
│  └──────────┘  └──────────────┘  └───────────────┘  │
└─────────────────────────────────────────────────────┘
```

依赖关系：
- `domain` 模块 → 定义聚合根 `SerialNumberRule`、值对象 `Segment`/`SegmentType`
- `domain` 模块 → 定义领域服务接口 `SerialNumberDomainService`
- `infrastructure` 模块 → 实现 `SerialNumberDomainServiceImpl`（Redisson RAtomicLong + 分布式锁）
- `app` 模块 → 应用服务 `SerialNumberApplicationService`（对外编排）
- `rest` 模块 → REST 控制器 `SerialNumberController`

### 1.2 聚合根与实体清单

| 类名 | 类型 | 所属模块 | 说明 |
|------|------|----------|------|
| `SerialNumberRule` | 聚合根 | `domain/.../serialnumber/aggregate/` | 编码规则定义，包含 segment 列表 |
| `Segment` | 值对象 | `domain/.../serialnumber/valueobject/` | 编码片段，包含类型和参数 |
| `SegmentType` | 枚举 | `domain/.../serialnumber/valueobject/` | FIXED, DATE, SEQUENCE |
| `ResetStrategy` | 枚举 | `domain/.../serialnumber/valueobject/` | DAILY, MONTHLY, YEARLY, NEVER |
| `SerialNumberRulePO` | 持久化实体 | `infrastructure/.../entity/serialnumber/` | JPA 映射表 `T_OPEN_SERIAL_NUMBER_RULE_` |
| `SerialNumberDomainService` | 领域服务接口 | `domain/service/` | 核心生成逻辑接口 |
| `SerialNumberDomainServiceImpl` | 领域服务实现 | `infrastructure/.../service/impl/` | Redisson 实现 |

### 1.3 核心流程时序图（文本）

```
用户调用生成编码
  │
  ▼
SerialNumberController.generate(ruleCode)
  │
  ▼
SerialNumberApplicationService.generateNext(ruleCode)
  │
  ├─(1) 查询规则定义
  │    SerialNumberRuleRepository.findByRuleCode(ruleCode)
  │    → 获取 SerialNumberRule 聚合根（含 Segment 列表）
  │
  ├─(2) 遍历 Segment 列表，逐个生成片段值
  │    for each segment:
  │      ├─ FIXED  → 直接返回固定字符串
  │      ├─ DATE   → SimpleDateFormat 格式化当前日期
  │      └─ SEQUENCE →
  │           │
  │           ├─ 计算 Redis Key:
  │           │   serial:{ruleCode}:{dateSegmentValue}
  │           │   (如 serial:INV:20250615)
  │           │
  │           ├─ 获取 RAtomicLong: redisUtil.rAtomicLong(key)
  │           │
  │           ├─ 获取分布式锁: RLock lock = redisson.getLock(key + ":lock")
  │           │   lock.tryLock(5, TimeUnit.SECONDS)
  │           │
  │           ├─ 读取当前值: long current = atomicLong.incrementAndGet()
  │           │
  │           ├─ 溢出检查:
  │           │   if (current > maxValue) → throw SerialNumberOverflowException
  │           │
  │           ├─ 重置检查:
  │           │   if (needReset) → atomicLong.set(0); 重新 increment
  │           │
  │           ├─ 格式化: String.format("%0" + digits + "d", current)
  │           │
  │           └─ 释放锁: lock.unlock()
  │
  ├─(3) 拼接所有片段 → 最终编码字符串
  │
  └─ 返回编码
```

### 1.4 数据库表结构草案

```sql
-- 编码规则定义表
CREATE TABLE T_OPEN_SERIAL_NUMBER_RULE_ (
    id_              VARCHAR(64)   NOT NULL PRIMARY KEY COMMENT '主键',
    rule_code_       VARCHAR(64)   NOT NULL UNIQUE COMMENT '规则编码，如 INV',
    rule_name_       VARCHAR(128)  NOT NULL COMMENT '规则名称',
    template_        TEXT          NOT NULL COMMENT '模板JSON，存储Segment列表',
    active_          TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否启用',
    version_         INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time_     DATETIME      NOT NULL COMMENT '创建时间',
    update_time_     DATETIME      NOT NULL COMMENT '更新时间',
    INDEX idx_rule_code (rule_code_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编码规则表';
```

**template_ 字段 JSON 示例：**
```json
[
  {"type":"FIXED","value":"INV-"},
  {"type":"DATE","format":"yyyyMMdd"},
  {"type":"SEQUENCE","digits":4,"resetStrategy":"DAILY","step":1}
]
```

### 1.5 API 接口契约

#### 1.5.1 生成编码

```
POST /api/v1/serial-numbers/generate
Content-Type: application/json

Request:
{
  "ruleCode": "INV"
}

Response 200:
{
  "code": 0,
  "message": "success",
  "data": {
    "serialNumber": "INV-202506150001",
    "ruleCode": "INV",
    "generatedAt": "2025-06-15T10:30:00"
  }
}

Response 400 (溢出):
{
  "code": "SERIAL_001",
  "message": "序列号已超过最大位数限制: ruleCode=INV, digits=4, maxValue=9999"
}
```

#### 1.5.2 管理规则 CRUD

```
GET    /api/v1/serial-number-rules              → 分页查询规则列表
GET    /api/v1/serial-number-rules/{id}         → 查询规则详情
POST   /api/v1/serial-number-rules              → 创建规则
PUT    /api/v1/serial-number-rules/{id}         → 更新规则（携带version做乐观锁）
DELETE /api/v1/serial-number-rules/{id}         → 删除规则
PATCH  /api/v1/serial-number-rules/{id}/toggle  → 启用/禁用规则
```

**创建规则请求体：**
```json
{
  "ruleCode": "INV",
  "ruleName": "发票编号规则",
  "segments": [
    {"type": "FIXED", "value": "INV-"},
    {"type": "DATE", "format": "yyyyMMdd"},
    {"type": "SEQUENCE", "digits": 4, "resetStrategy": "DAILY", "step": 1}
  ]
}
```

### 1.6 非功能性设计决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| 序列号生成并发控制 | `RAtomicLong` + 分布式锁（Redisson `RLock`）双层保障 | 集群环境无缺口、无重复 |
| Redis Key 设计 | `serial:{ruleCode}:{dateSegment}` | 支持按日/月/年自动隔离，重置策略天然实现 |
| 锁超时 | 5 秒 | 防止死锁，序列号生成操作极快 |
| 溢出处理 | 抛 `SerialNumberOverflowException` 业务异常 | 静默截断会导致业务数据错误 |
| 乐观锁 | `version` 字段 + JPA `@Version` | 防止并发修改规则配置冲突 |
| Segment JSON 持久化 | `template_` TEXT 字段存 JSON | 灵活支持未来扩展新片段类型 |

### 1.7 风险点

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Redis 不可用导致编码生成失败 | 高 | 增加本地降级方案（基于数据库自增 + 分布式锁）；监控 Redis 可用性 |
| 日期回拨导致编码重复 | 中 | 日期片段在 Redis Key 中，天然不会重复；但仍需 NTP 时间同步 |
| 高并发下锁竞争激烈 | 中 | 批量预取序列号（一次取 10 个），减少 Redis 交互 |
| 规则误删导致历史编码无法追溯 | 低 | 软删除（active=false），保留规则记录 |

---

## P0-2: Bouncy Castle 加密模块

### 2.1 领域边界划分

```
┌──────────────────────────────────────────────────────────┐
│  Bounded Context: Cryptography (加密服务)                  │
│                                                            │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐  │
│  │ AES-256  │ │ RSA-4096 │ │ SM2/SM4  │ │ RSA+AES    │  │
│  │ GCM      │ │ OAEP     │ │ (国密)   │ │ Hybrid     │  │
│  └──────────┘ └──────────┘ └──────────┘ └────────────┘  │
└──────────────────────────────────────────────────────────┘
```

该模块为纯基础设施层服务，不涉及业务领域模型。放在 `infrastructure` 模块的 `crypto` 包下。

### 2.2 模块结构与类清单

```
infrastructure/src/main/java/com/wsf/infrastructure/crypto/
├── CryptoService.java                    # 统一加密服务接口
├── impl/
│   ├── AesGcmCryptoService.java          # AES-256-GCM 加解密
│   ├── RsaCryptoService.java             # RSA-2048/4096 加解密+签名
│   ├── Sm2CryptoService.java             # SM2 国密非对称
│   ├── Sm4CryptoService.java             # SM4 国密对称
│   └── RsaAesHybridCryptoService.java    # RSA+AES 混合加密
├── key/
│   ├── KeyPairGenerator.java             # 密钥对生成工具
│   └── KeyStorageService.java            # 密钥持久化（可扩展至KMS）
├── config/
│   └── CryptoConfiguration.java          # 加密模块自动配置
└── exception/
    ├── CryptoException.java              # 加密异常基类
    └── KeyManagementException.java       # 密钥管理异常
```

### 2.3 核心流程时序图（文本）—— RSA+AES 混合加密

```
发送方加密流程：
  │
  ├─(1) 生成随机 AES-256 会话密钥
  │    KeyGenerator keyGen = KeyGenerator.getInstance("AES")
  │    keyGen.init(256)
  │    SecretKey sessionKey = keyGen.generateKey()
  │
  ├─(2) 使用 AES-256-GCM 加密明文
  │    Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
  │    aesCipher.init(ENCRYPT_MODE, sessionKey)
  │    byte[] iv = aesCipher.getIV()           // 12字节随机IV
  │    byte[] ciphertext = aesCipher.doFinal(plaintext)
  │
  ├─(3) 使用接收方 RSA 公钥加密 AES 会话密钥
  │    Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
  │    rsaCipher.init(ENCRYPT_MODE, receiverPublicKey)
  │    byte[] encryptedSessionKey = rsaCipher.doFinal(sessionKey.getEncoded())
  │
  └─(4) 组装密文包: iv + encryptedSessionKey + ciphertext
       → 返回 HybridCiphertext { iv, encryptedKey, ciphertext }

接收方解密流程：
  │
  ├─(1) 解析密文包 → 提取 iv, encryptedKey, ciphertext
  │
  ├─(2) 使用 RSA 私钥解密 AES 会话密钥
  │    Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
  │    rsaCipher.init(DECRYPT_MODE, receiverPrivateKey)
  │    byte[] sessionKeyBytes = rsaCipher.doFinal(encryptedKey)
  │    SecretKey sessionKey = new SecretKeySpec(sessionKeyBytes, "AES")
  │
  ├─(3) 使用 AES-256-GCM 解密密文
  │    Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
  │    GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv)
  │    aesCipher.init(DECRYPT_MODE, sessionKey, gcmSpec)
  │    byte[] plaintext = aesCipher.doFinal(ciphertext)
  │
  └─(4) 返回明文
```

### 2.4 API 接口契约

#### 2.4.1 加密服务接口 `CryptoService`

```java
public interface CryptoService {

    /** AES-256-GCM 加密 */
    AesCiphertext aesEncrypt(byte[] plaintext);

    /** AES-256-GCM 解密 */
    byte[] aesDecrypt(AesCiphertext ciphertext);

    /** RSA 公钥加密 */
    byte[] rsaEncrypt(byte[] plaintext, PublicKey publicKey);

    /** RSA 私钥解密 */
    byte[] rsaDecrypt(byte[] ciphertext, PrivateKey privateKey);

    /** RSA 签名 */
    byte[] rsaSign(byte[] data, PrivateKey privateKey);

    /** RSA 验签 */
    boolean rsaVerify(byte[] data, byte[] signature, PublicKey publicKey);

    /** SM2 加密 */
    byte[] sm2Encrypt(byte[] plaintext, PublicKey publicKey);

    /** SM2 解密 */
    byte[] sm2Decrypt(byte[] ciphertext, PrivateKey privateKey);

    /** SM4 加密 */
    Sm4Ciphertext sm4Encrypt(byte[] plaintext);

    /** SM4 解密 */
    byte[] sm4Decrypt(Sm4Ciphertext ciphertext);

    /** RSA+AES 混合加密 */
    HybridCiphertext hybridEncrypt(byte[] plaintext, PublicKey receiverPublicKey);

    /** RSA+AES 混合解密 */
    byte[] hybridDecrypt(HybridCiphertext ciphertext, PrivateKey receiverPrivateKey);

    /** 生成 RSA 密钥对 */
    KeyPair generateRsaKeyPair(int keySize); // 2048 or 4096

    /** 生成 SM2 密钥对 */
    KeyPair generateSm2KeyPair();
}
```

#### 2.4.2 REST 接口（测试/管理用）

```
POST /api/v1/crypto/aes/encrypt          → AES加密
POST /api/v1/crypto/aes/decrypt          → AES解密
POST /api/v1/crypto/rsa/encrypt          → RSA加密
POST /api/v1/crypto/rsa/decrypt          → RSA解密
POST /api/v1/crypto/rsa/sign             → RSA签名
POST /api/v1/crypto/rsa/verify           → RSA验签
POST /api/v1/crypto/sm2/encrypt          → SM2加密
POST /api/v1/crypto/sm2/decrypt          → SM2解密
POST /api/v1/crypto/sm4/encrypt          → SM4加密
POST /api/v1/crypto/sm4/decrypt          → SM4解密
POST /api/v1/crypto/hybrid/encrypt       → RSA+AES混合加密
POST /api/v1/crypto/hybrid/decrypt       → RSA+AES混合解密
POST /api/v1/crypto/keys/rsa/generate    → 生成RSA密钥对
POST /api/v1/crypto/keys/sm2/generate    → 生成SM2密钥对
```

### 2.5 非功能性设计决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| AES 模式 | AES-256-GCM (Galois/Counter Mode) | 提供认证加密（AEAD），防止密文篡改 |
| RSA 填充 | OAEPWithSHA-256AndMGF1Padding | 安全性优于 PKCS1Padding，防选择密文攻击 |
| RSA 密钥长度 | 默认 4096，可配置 2048 | 符合规范要求；4096 更安全但性能略低 |
| SM2/SM4 实现 | Bouncy Castle `SM2Engine` / `SM4Engine` | 国密标准，BC 1.78 原生支持 |
| 密钥管理 | 内部 `KeyStorageService`（初始版本文件存储） | 后续可对接 KMS（阿里云/HashiCorp Vault） |
| IV 生成 | `SecureRandom` 每次加密生成随机 IV | GCM 要求 IV 不重复 |

### 2.6 风险点

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Bouncy Castle SM2 曲线参数不兼容 | 中 | 固定使用 `sm2p256v1` 曲线；单元测试与 OpenSSL 交叉验证 |
| 密钥泄露 | 高 | 生产环境强制使用 KMS；本地文件密钥加密存储 |
| RSA 4096 加解密性能 | 低 | 混合加密方案已解决：只需加密 256-bit AES 密钥 |
| GCM IV 重复导致安全性破坏 | 高 | 使用 `SecureRandom` 生成 12 字节 IV；记录 IV 使用日志 |

---

## P0-3: REST API 控制器层

### 3.1 控制器清单与 URI 设计

```
rest/src/main/java/com/wsf/controller/
├── UserController.java              → /api/v1/users
├── RoleController.java              → /api/v1/roles
├── MenuController.java              → /api/v1/menus
├── PermissionController.java        → /api/v1/permissions
├── DataPermissionController.java    → /api/v1/data-permissions
├── AccountController.java           → /api/v1/accounts
├── AuthController.java              → /api/v1/auth (重构LoginController)
└── CryptoController.java            → /api/v1/crypto
```

### 3.2 完整 REST API 契约

#### UserController: `/api/v1/users`

```
POST   /api/v1/users                          创建用户
GET    /api/v1/users                          分页查询用户列表 (?page=0&size=20&keyword=&sort=createTime,desc)
GET    /api/v1/users/{userId}                 查询用户详情
PUT    /api/v1/users/{userId}                 更新用户
DELETE /api/v1/users/{userId}                 删除用户
POST   /api/v1/users/{userId}/account         为用户创建登录账户
DELETE /api/v1/users/{userId}/account         解除用户与账户关联
```

**创建用户请求/响应：**
```json
// POST /api/v1/users
Request:
{
  "firstName": "三",
  "lastName": "张",
  "email": "zhangsan@example.com",
  "phoneNumber": "13800138000",
  "idCardNumber": "110101199001011234",
  "createAccount": true,
  "username": "zhangsan",
  "password": "Abc@123456"
}

Response 201:
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "U_abc123",
    "firstName": "三",
    "lastName": "张",
    "fullName": "张三",
    "email": "zhangsan@example.com",
    "phoneNumber": "13800138000",
    "hasAccount": true,
    "accountId": "A_xyz789",
    "username": "zhangsan",
    "createTime": "2025-06-15T10:00:00",
    "updateTime": "2025-06-15T10:00:00"
  }
}
```

#### RoleController: `/api/v1/roles`

```
POST   /api/v1/roles                          创建角色
GET    /api/v1/roles                          查询所有角色
GET    /api/v1/roles/{roleId}                 查询角色详情（含菜单和数据权限）
PUT    /api/v1/roles/{roleId}                 更新角色
DELETE /api/v1/roles/{roleId}                 删除角色
PATCH  /api/v1/roles/{roleId}/enable          启用角色
PATCH  /api/v1/roles/{roleId}/disable         禁用角色
PUT    /api/v1/roles/{roleId}/menus           分配菜单（请求体: { "menuIds": ["M1","M2"] }）
PUT    /api/v1/roles/{roleId}/data-permissions  分配数据权限
```

**角色详情响应（含关联）：**
```json
{
  "id": "R_admin",
  "code": "ADMIN",
  "name": "管理员",
  "description": "系统管理员，拥有所有权限",
  "enabled": true,
  "menuIds": ["M_sys", "M_user", "M_role"],
  "dataPermissionIds": ["DP_all"],
  "createTime": "2025-01-01T00:00:00",
  "updateTime": "2025-06-15T10:00:00"
}
```

#### MenuController: `/api/v1/menus`

```
POST   /api/v1/menus                          创建菜单/按钮
GET    /api/v1/menus                          获取菜单树（?includeButtons=true）
GET    /api/v1/menus/{menuId}                 查询菜单详情
PUT    /api/v1/menus/{menuId}                 更新菜单
DELETE /api/v1/menus/{menuId}                 删除菜单（有子菜单时拒绝）
PATCH  /api/v1/menus/{menuId}/enable          启用
PATCH  /api/v1/menus/{menuId}/disable         禁用
PATCH  /api/v1/menus/{menuId}/show            显示
PATCH  /api/v1/menus/{menuId}/hide            隐藏
```

**创建按钮请求：**
```json
{
  "name": "删除用户",
  "parentId": "M_user",
  "menuType": "BUTTON",
  "permission": "system:delete:user",
  "sortOrder": 1
}
```

#### PermissionController: `/api/v1/permissions`

```
POST   /api/v1/permissions                    创建权限
GET    /api/v1/permissions                    查询权限列表 (?menuId=&enabled=)
GET    /api/v1/permissions/{permissionId}     查询权限详情
PUT    /api/v1/permissions/{permissionId}     更新权限
DELETE /api/v1/permissions/{permissionId}     删除权限
PATCH  /api/v1/permissions/{permissionId}/enable   启用
PATCH  /api/v1/permissions/{permissionId}/disable  禁用
```

#### DataPermissionController: `/api/v1/data-permissions`

```
POST   /api/v1/data-permissions                    创建数据权限
GET    /api/v1/data-permissions                    查询列表
GET    /api/v1/data-permissions/{id}               查询详情
PUT    /api/v1/data-permissions/{id}               更新
PUT    /api/v1/data-permissions/{id}/resource-ids  更新自定义资源ID
DELETE /api/v1/data-permissions/{id}               删除
PATCH  /api/v1/data-permissions/{id}/enable        启用
PATCH  /api/v1/data-permissions/{id}/disable       禁用
```

#### AccountController: `/api/v1/accounts`

```
GET    /api/v1/accounts/{accountId}           查询账户详情（含角色列表）
PUT    /api/v1/accounts/{accountId}/roles     分配角色 (?roleIds=R1,R2)
PUT    /api/v1/accounts/{accountId}/password  修改密码
PATCH  /api/v1/accounts/{accountId}/enable    启用账户
PATCH  /api/v1/accounts/{accountId}/disable   禁用账户
PATCH  /api/v1/accounts/{accountId}/lock      锁定账户（登录失败超限）
PATCH  /api/v1/accounts/{accountId}/unlock    解锁账户
```

#### AuthController: `/api/v1/auth` (重构现有 LoginController)

```
POST   /api/v1/auth/login                    用户名密码登录
POST   /api/v1/auth/logout                   退出登录
POST   /api/v1/auth/refresh                  刷新Token
GET    /api/v1/auth/me                       获取当前登录用户信息+权限列表
GET    /api/v1/auth/menus                    获取当前用户菜单树
```

### 3.3 统一响应格式

```json
// 成功
{ "code": 0, "message": "success", "data": {...} }

// 分页
{ "code": 0, "message": "success", "data": { "content": [...], "page": 0, "size": 20, "totalElements": 100, "totalPages": 5 } }

// 业务异常
{ "code": "BIZ_001", "message": "用户名已存在", "data": null }

// 权限不足
{ "code": 403, "message": "Access Denied", "data": null }
```

### 3.4 非功能性决策

| 决策点 | 方案 |
|--------|------|
| 参数校验 | `jakarta.validation` 注解 + `@Validated` |
| 分页 | Spring Data `Pageable`，默认 page=0, size=20, sort=createTime,desc |
| 权限校验 | Controller 方法上 `@PreAuthorize("@ss.hasPermission('system:delete:user')")` |
| API 文档 | Knife4j `@Operation` + `@Parameter` 注解自动生成 |
| DTO 映射 | MapStruct Converter（复用现有模式） |

---

## P0-4: @DataScope 注解 + QueryDSL 拦截器

### 4.1 核心设计

```
┌──────────────────────────────────────────────────────────┐
│                 数据权限拦截流程                            │
│                                                            │
│  Controller 方法标注 @DataScope                            │
│       │                                                    │
│       ▼                                                    │
│  DataScopeAspect (AOP 切面)                                │
│       │                                                    │
│       ├─(1) 从 SecurityContext 获取当前用户                 │
│       ├─(2) 查询用户角色关联的 DataPermission               │
│       ├─(3) 根据 DataScope 类型计算过滤条件                  │
│       │     ├─ ALL          → 不添加过滤                   │
│       │     ├─ DEPT_ONLY    → eq(deptId, user.deptId)     │
│       │     ├─ DEPT_AND_SUB → in(deptId, subDeptIds)      │
│       │     ├─ CUSTOM       → in(deptId, customDeptIds)   │
│       │     └─ SELF_ONLY    → eq(createBy, userId)        │
│       ├─(4) 将过滤条件注入 ThreadLocal<Predicate>           │
│       └─(5) 执行目标方法                                    │
│                                                            │
│  QueryDSL Repository 方法内:                                │
│       │                                                    │
│       ├─ 从 ThreadLocal 获取 Predicate                     │
│       ├─ 合并到 QueryDSL 查询链: .where(dataScopePredicate) │
│       └─ 执行查询                                          │
└──────────────────────────────────────────────────────────┘
```

### 4.2 核心类清单

```
domain/src/main/java/com/wsf/domain/annotation/
└── DataScope.java                        # @DataScope 注解定义

infrastructure/src/main/java/com/wsf/infrastructure/datascope/
├── DataScopeAspect.java                  # AOP 切面，解析注解并计算过滤条件
├── DataScopeContext.java                 # ThreadLocal 持有当前请求的过滤 Predicate
├── DataScopeFilterService.java          # 根据 DataScope 枚举构建 QueryDSL Predicate
└── DataScopeQueryInterceptor.java       # JPA QueryDSL 拦截器（自动拼接条件）
```

### 4.3 注解定义

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    /**
     * 数据权限切入点，对应 DataPermission 的 resourceType
     * 如 DEPT, USER, ORDER 等
     */
    String resourceType() default "DEPT";

    /**
     * 实体中部门ID字段的 QueryDSL 路径表达式
     * 如 "deptId" 对应 QUser.user.deptId
     */
    String deptColumn() default "deptId";

    /**
     * 实体中创建人字段的 QueryDSL 路径表达式
     * 如 "createBy" 对应 QUser.user.createBy
     */
    String userColumn() default "createBy";
}
```

使用示例：
```java
@GetMapping("/users")
@DataScope(resourceType = "DEPT", deptColumn = "deptId", userColumn = "createBy")
public ResponseEntity<Page<UserDto>> listUsers(Pageable pageable) {
    // QueryDSL 拦截器自动叠加数据权限过滤条件
    return ResponseEntity.ok(userService.findAll(pageable));
}
```

### 4.4 时序流程（文本）

```
HTTP GET /api/v1/users
  │
  ▼
UserController.listUsers()   ← @DataScope(resourceType="DEPT", deptColumn="deptId", userColumn="createBy")
  │
  ▼
DataScopeAspect.before()
  │
  ├─(1) 获取当前认证用户
  │    Authentication auth = SecurityContextHolder.getContext().getAuthentication()
  │    String userId = auth.getName()
  │
  ├─(2) 从缓存/数据库获取用户的 DataPermission 列表
  │    List<DataPermission> permissions = dataPermissionCacheService.getUserPermissions(userId, "DEPT")
  │
  ├─(3) 找到最高优先级的 DataScope (按 level 升序)
  │    DataScope effectiveScope = permissions.stream()
  │        .map(DataPermission::getDataScope)
  │        .min(Comparator.comparing(DataScope::getLevel))
  │        .orElse(DataScope.SELF)  // 默认仅本人
  │
  ├─(4) 构建 QueryDSL Predicate
  │    switch (effectiveScope):
  │      ALL          → null (不过滤)
  │      DEPT_ONLY    → QUser.user.deptId.eq(currentUserDeptId)
  │      DEPT_AND_SUB → QUser.user.deptId.in(currentUserDeptIds + subDeptIds)
  │      SELF_ONLY    → QUser.user.createBy.eq(userId)
  │      CUSTOM       → QUser.user.deptId.in(customDeptIds)
  │
  ├─(5) 存入 ThreadLocal
  │    DataScopeContext.set(predicate)
  │
  ▼
UserService.findAll(pageable)
  │
  ▼
UserRepository.findAll(pageable)
  │
  ├─ DataScopeQueryInterceptor 从 ThreadLocal 获取 Predicate
  ├─ 合并到 JPAQuery: jpaQueryFactory.selectFrom(QUser.user).where(dataScopePred).offset(...).limit(...)
  └─ 返回过滤后的结果
  │
  ▼
DataScopeAspect.after()
  │
  └─ DataScopeContext.clear()
```

### 4.5 数据模型增强

需要在现有实体中增加数据权限所需字段（若不存在）：

**User 表新增：**
```sql
ALTER TABLE T_OPEN_USER_ ADD COLUMN dept_id_ VARCHAR(64) COMMENT '所属部门ID';
ALTER TABLE T_OPEN_USER_ ADD COLUMN create_by_ VARCHAR(64) COMMENT '创建人ID';
```

**新增部门表（P2，可先简化）：**
```sql
CREATE TABLE T_OPEN_DEPT_ (
    id_          VARCHAR(64) NOT NULL PRIMARY KEY,
    parent_id_   VARCHAR(64) COMMENT '上级部门ID',
    dept_name_   VARCHAR(128) NOT NULL,
    ancestors_   VARCHAR(512) COMMENT '祖级列表（逗号分隔）',
    sort_order_  INT DEFAULT 0,
    create_time_ DATETIME NOT NULL,
    update_time_ DATETIME NOT NULL
) COMMENT='部门表';
```

### 4.6 非功能性决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| ThreadLocal 清理 | AOP `@After` 切面中 `finally` 块清理 | 防止内存泄漏，避免影响后续请求 |
| Predicate 合并策略 | AND 逻辑合并 | 数据权限为「最小权限原则」的叠加 |
| 缓存 | 从 Redisson 权限缓存读取（见 P0-6） | 避免每次请求查询数据库 |
| 注解默认值 | 不添加 @DataScope 的接口默认不过滤 | 显式声明，避免误伤 |

### 4.7 风险点

| 风险 | 缓解措施 |
|------|----------|
| ThreadLocal 未清理导致内存泄漏或脏数据 | AOP `@After` + `finally` 双重保障 |
| 异步方法中 ThreadLocal 不可用 | @DataScope 不支持 @Async 方法 |
| 复杂 JOIN 查询无法应用 Predicate | 仅在主实体上应用过滤，关联查询需单独处理 |
| 超级管理员被误过滤 | ALL 权限返回 null Predicate，不做任何过滤 |

---

## P0-5: 按钮权限模型 + @PreAuthorize

### 5.1 权限码规范

**格式：** `模块:操作:资源`

```
system:create:user       → 创建用户
system:delete:user       → 删除用户
system:update:user       → 修改用户
system:view:user         → 查看用户
system:create:role       → 创建角色
system:delete:role       → 删除角色
system:assign:menu       → 分配菜单
system:assign:dataPerm   → 分配数据权限
monitor:view:log         → 查看日志
```

### 5.2 Permission 表结构调整

当前 [`Permission`](domain/src/main/java/com/wsf/domain/model/permission/entity/Permission.java:12) 实体字段 `code` / `resource` / `action` 需要重新定义：

**调整后的 Permission 字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 主键 |
| `code` | String | 权限码，如 `system:delete:user` |
| `name` | String | 权限名称，如「删除用户」 |
| `type` | Enum(MENU\|BUTTON\|API) | 权限类型 |
| `parentId` | String | 父级ID（支持树形） |
| `menuId` | String | 关联菜单ID（type=BUTTON时必填） |
| `path` | String | 接口路径（type=API时） |
| `method` | String | HTTP方法（type=API时） |
| `sort` | Integer | 排序 |
| `enabled` | Boolean | 是否启用 |

### 5.3 Permission 持久化 PO

```java
@Entity
@Table(name = "T_OPEN_PERMISSION_")
public class PermissionPO extends BaseEntity {
    @Column(name = "code_", nullable = false, unique = true)
    private String code;

    @Column(name = "name_", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_", nullable = false)
    private PermissionType type; // MENU, BUTTON, API

    @Column(name = "parent_id_")
    private String parentId;

    @Column(name = "menu_id_")
    private String menuId;

    @Column(name = "path_")
    private String path;

    @Column(name = "method_")
    private String method;

    @Column(name = "sort_")
    private Integer sort;

    @Column(name = "enabled_")
    private Boolean enabled;
}
```

### 5.4 Role-Permission 关联

```
┌──────────┐     ┌────────────────────┐     ┌──────────────┐
│  Role    │────<│ Role_Permission    │>────│  Permission  │
│          │     │ role_id            │     │              │
│          │     │ permission_id      │     │              │
└──────────┘     └────────────────────┘     └──────────────┘
```

新增 `T_OPEN_ROLE_PERMISSION_` 中间表（替代当前 Role-Menu 直接多对多，Menu 改为通过 Permission 间接关联）。

### 5.5 @PreAuthorize 校验实现

**自定义 PermissionEvaluator：**

```java
@Component("ss")
public class SecurityService {

    public boolean hasPermission(String permissionCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        // 从缓存获取用户权限码集合
        Set<String> permissions = permissionCacheService.getUserPermissions(auth.getName());
        return permissions.contains(permissionCode) || permissions.contains("admin:*");
    }

    public boolean hasAnyPermission(String... permissionCodes) {
        return Arrays.stream(permissionCodes).anyMatch(this::hasPermission);
    }
}
```

**Controller 层使用：**

```java
@DeleteMapping("/users/{userId}")
@PreAuthorize("@ss.hasPermission('system:delete:user')")
public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
    userService.deleteUser(userId);
    return ResponseEntity.noContent().build();
}
```

### 5.6 前端按钮权限传递

登录成功后返回权限码列表：

```json
// GET /api/v1/auth/me
{
  "username": "zhangsan",
  "roles": ["ADMIN"],
  "permissions": [
    "system:create:user",
    "system:update:user",
    "system:delete:user",
    "system:view:user",
    "menu:dashboard",
    "menu:system:user"
  ]
}
```

前端通过 `v-permission="system:delete:user"` 指令控制按钮显隐。

### 5.7 非功能性决策

| 决策点 | 方案 |
|--------|------|
| 超级管理员 | 权限码 `admin:*` 匹配所有权限 |
| 权限码校验缓存 | P0-6 方案：Redisson 缓存 30 分钟 + pub/sub 失效 |
| @PreAuthorize SpEL | 使用自定义 Bean `@ss` 注入 SecurityService |
| 权限类型区分 | MENU=菜单可见性, BUTTON=按钮显隐, API=接口鉴权 |

---

## P0-6: 权限缓存 + Redisson 发布/订阅

### 6.1 架构设计

```
                    ┌─────────────────────┐
                    │   Redisson Topic     │
                    │  "perm:cache:evict"  │
                    └──┬────────┬──────────┘
                       │        │
              publish  │        │  subscribe
                       │        │
        ┌──────────────▼──┐  ┌──▼──────────────┐
        │  节点 A          │  │  节点 B          │
        │  ┌────────────┐ │  │  ┌────────────┐ │
        │  │ 本地缓存    │ │  │  │ 本地缓存    │ │
        │  │ (Redisson) │ │  │  │ (Redisson) │ │
        │  └────────────┘ │  │  └────────────┘ │
        └─────────────────┘  └─────────────────┘
```

### 6.2 缓存 Key 设计

| 缓存对象 | Key 格式 | TTL | 说明 |
|----------|----------|-----|------|
| 用户权限码列表 | `perm:user:{userId}` | 30分钟 | `Set<String>` 权限码集合 |
| 用户角色列表 | `perm:user:{userId}:roles` | 30分钟 | `Set<String>` 角色ID集合 |
| 用户数据权限 | `perm:user:{userId}:data:{resourceType}` | 30分钟 | 用户在某资源类型下的 DataScope |
| 角色-权限映射 | `perm:role:{roleId}` | 60分钟 | 角色关联的权限码集合 |
| 全部权限码 | `perm:all` | 60分钟 | 系统全部权限码（用于校验合法性） |

### 6.3 模块清单

```
infrastructure/src/main/java/com/wsf/infrastructure/cache/
├── PermissionCacheService.java              # 权限缓存服务接口
├── impl/
│   └── RedissonPermissionCacheServiceImpl.java  # Redisson 实现
├── listener/
│   └── PermissionCacheEvictListener.java    # 订阅 perm:cache:evict 频道
└── config/
    └── PermissionCacheConfiguration.java    # 缓存自动配置
```

### 6.4 发布/订阅消息格式

```json
{
  "action": "EVICT",
  "cacheKey": "perm:user:U001",
  "timestamp": "2025-06-15T10:30:00Z",
  "sourceNode": "node-a"
}
```

订阅者收到消息后，删除本地 Redisson 缓存中对应 Key。

### 6.5 缓存加载与失效流程（文本）

```
【缓存加载】
  │
  ├─ 请求到达 → 需要用户权限
  │
  ├─ PermissionCacheService.getUserPermissions(userId)
  │    │
  │    ├─(1) 查 Redisson: RBucket<Set<String>> bucket = redisson.getBucket("perm:user:" + userId)
  │    │    if (bucket.isExists()) → return bucket.get()
  │    │
  │    ├─(2) 缓存未命中 → 查数据库
  │    │    ├─ 查 user-role 关联 → 角色列表
  │    │    ├─ 查 role-permission 关联 → 权限码集合
  │    │    └─ 合并去重 → Set<String> permissions
  │    │
  │    └─(3) 写入缓存: bucket.set(permissions, 30, TimeUnit.MINUTES)
  │
  └─ 返回权限集合

【缓存失效】
  │
  ├─ 管理端修改角色-权限关联
  │
  ├─ RoleService.assignPermissions(roleId, permissionIds)
  │    │
  │    ├─(1) 更新数据库
  │    ├─(2) 清除角色缓存: redisson.getBucket("perm:role:" + roleId).delete()
  │    ├─(3) 查找受影响的用户ID列表
  │    ├─(4) 逐个清除用户缓存: redisson.getBucket("perm:user:" + userId).delete()
  │    │
  │    └─(5) 发布广播: topic.publish(EVICT消息)
  │
  └─ 所有节点收到广播 → 清除本地对应缓存
```

### 6.6 Redisson Topic 配置

```java
@Configuration
public class PermissionCacheConfiguration {

    @Bean
    public RTopic permissionCacheEvictTopic(RedissonClient redissonClient) {
        return redissonClient.getTopic("perm:cache:evict");
    }

    @Bean
    public PermissionCacheEvictListener evictListener(RTopic topic, RedissonClient redissonClient) {
        PermissionCacheEvictListener listener = new PermissionCacheEvictListener(redissonClient);
        topic.addListener(CacheEvictMessage.class, listener);
        return listener;
    }
}
```

### 6.7 非功能性决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| 缓存存储 | Redisson `RBucket<Set<String>>` | 简单高效，支持 TTL |
| 序列化 | `JsonJacksonCodec`（Redisson 配置已全局设置） | 与现有配置一致 |
| 缓存预热 | 应用启动时预加载 `perm:all` | 减少首次请求延迟 |
| 缓存穿透 | 空集合也缓存（TTL 1 分钟） | 防止恶意查询击穿数据库 |
| 缓存雪崩 | TTL 设置随机偏移 (±5 分钟) | 避免大量 Key 同时过期 |

### 6.8 风险点

| 风险 | 缓解措施 |
|------|----------|
| pub/sub 消息丢失 | 管理端操作同时主动删除缓存 + 广播，双写策略 |
| 新节点未收到历史广播 | 启动时不做预加载，采用懒加载 + 短 TTL |
| 缓存与数据库不一致 | 30 分钟 TTL 足够短；关键操作（修改角色）立即失效 |

---

## 整体架构调整建议

### 当前模块依赖关系 vs 建议调整

```
当前:  start → rest → infrastructure → domain
              ↘ system ↗          ↘ domain

建议:  start → rest → app → api
              ↘ system        ↓
                           domain ← infrastructure (实现 repository)
```

**主要调整：**
1. [`rest`](rest/pom.xml:12) 模块应从依赖 [`infrastructure`](infrastructure/pom.xml:12) 改为依赖 [`app`](app/pom.xml:12)（应用层）
2. [`system`](system/pom.xml:12) 模块应重构合并到 `app` 或 `rest`，避免概念混淆
3. [`adapter`](adapter/pom.xml:12) 模块应实现具体的外部服务适配器（如 SMS、邮件、OSS）

### 新增/修改文件总计

| 模块 | 新增 | 修改 | 说明 |
|------|------|------|------|
| `domain` | ~15 | ~5 | 序列号聚合根、Permission 实体调整、@DataScope 注解 |
| `infrastructure` | ~25 | ~10 | 序列号实现、加密模块、缓存模块、DataScope 拦截器 |
| `app` | ~10 | ~5 | 应用服务编排 |
| `api` | ~5 | ~8 | DTO 补充 |
| `rest` | ~12 | ~2 | 全部 CRUD 控制器 |
| **合计** | **~67** | **~30** | |

---

## 全局技术风险清单

| # | 风险描述 | 影响 | 概率 | 缓解措施 | 负责人 |
|---|---------|------|------|----------|--------|
| R1 | Bouncy Castle SM2/SM4 与国密标准不完全兼容 | 高 | 中 | 单元测试与 GmSSL/OpenSSL 交叉验证 |
| R2 | 编码规则引擎高并发下 Redis 锁超时 | 中 | 低 | 批量预取序列号 + 锁超时调优 |
| R3 | @DataScope 注解与 JPA 关联查询不兼容 | 中 | 中 | 限制仅主实体过滤，关联查询需显式声明 |
| R4 | pub/sub 消息跨节点延迟导致短暂权限不一致 | 低 | 中 | 接受最终一致性，30 分钟 TTL 兜底 |
| R5 | 大量 REST API 控制器增加启动时间和内存 | 低 | 低 | Spring Boot 懒加载 + 按需启用 |
| R6 | 加密模块密钥硬编码风险 | 高 | 低 | 密钥外部化配置 + 生产 KMS |

---

> **下一步：** 请逐项审查以上方案，确认后可切换至 Code 模式开始实施。

---

## 附录 A：与现有代码兼容性分析（实施前必读）

> 以下差异在实施 P0 前必须处理，否则会导致编译失败或运行时异常。

### A.1 依赖变更

| 模块 | 变更 | 原因 |
|------|------|------|
| [`infrastructure/pom.xml`](infrastructure/pom.xml) | 新增 `bcprov-jdk18on` + `bcpkix-jdk18on` 依赖 | P0-2 加密模块需要（父 pom 已声明版本，模块未引入） |
| [`rest/pom.xml`](rest/pom.xml) | 新增 `app` 模块依赖 | P0-3 控制器需要调用应用层服务 |

### A.2 SecurityConfig 调整

[`SecurityConfig`](infrastructure/src/main/java/com/wsf/infrastructure/security/config/SecurityConfig.java:49) 当前缺少 `@EnableMethodSecurity` 注解，导致 `@PreAuthorize` 无法生效。

```java
// 需添加注解
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ← 新增，启用 @PreAuthorize
```

### A.3 领域模型字段增强

| 实体 | 当前字段 | 需新增字段 | 关联 P0 项 |
|------|----------|-----------|-----------|
| [`User`](domain/src/main/java/com/wsf/domain/model/user/aggregate/User.java:13) | id, name, email, phone, idCard, realName | `deptId`(String), `createBy`(String) | P0-4 数据权限 |
| [`Permission`](domain/src/main/java/com/wsf/domain/model/permission/entity/Permission.java:12) | id, code, name, resource, action, menuId | `type`(PermissionType枚举), `parentId`, `path`, `method`, `sort` | P0-5 按钮权限 |
| [`Role`](domain/src/main/java/com/wsf/domain/model/role/aggregate/Role.java:17) | id, code, name, menus, dataPermissions | `permissions`(Set<Permission>) | P0-5 权限关联 |

### A.4 RedisUtil 激活

[`RedisUtil`](infrastructure/src/main/java/com/wsf/infrastructure/utils/RedisUtil.java:12) 的 `@Component` 被注释，P0-1 和 P0-6 依赖 Redis 操作。实施前需取消注释激活。

---

## 附录 B：修订后的实施顺序

```
阶段 1 — 基础设施（无业务依赖，可并行）
  │
  ├─ [P0-2] Bouncy Castle 加密模块（最独立，纯基础设施）
  │
  └─ [P0-6] 权限缓存 + Redisson 发布/订阅（激活 RedisUtil）

阶段 2 — 权限核心
  │
  ├─ 领域模型增强（User/Permission/Role 字段扩展）
  │
  └─ [P0-5] 按钮权限 + @PreAuthorize（依赖 P0-6 缓存 + @EnableMethodSecurity）

阶段 3 — 业务功能
  │
  ├─ [P0-1] 编码规则引擎（依赖 RedisUtil）
  │
  └─ [P0-4] @DataScope 数据权限拦截器（依赖阶段 2 的 User.deptId）

阶段 4 — 对外暴露
  │
  └─ [P0-3] REST API 控制器层（整合所有服务）
```

```text
infrastructure/                 # 基础设施层 - 技术实现
    ├── config/                     # 配置
    │   ├── database/
    │   ├── security/
    │   └── framework/
    ├── repository/                 # 仓储实现
    │   └── impl/
    ├── external/                   # 外部服务实现
    ├── mq/                         # 消息队列实现
    ├── cache/                      # 缓存实现
    └── common/                     # 通用工具
        ├── constants/              # 原constants
        ├── utils/                  # 原framework下的工具类
        ├── encrypt/                # 原encrypt
        └── exception/
```


### 3. **infrastructure** （基础设施层）

- 对应DDD层: 基础设施层（Infrastructure Layer）
- 职责: 为上层提供技术实现。包含:
    - `config/`: 各类技术配置（数据库、安全、框架）
    - `repository/impl/`: 仓储接口的具体实现（如 JPA Repository）
    - `external/`: 外部服务客户端（如调用第三方 API）
    - `mq/`: 消息队列实现
    - `cache/`: 缓存实现
    - `common/`: 通用工具、常量、异常等
- 依赖关系: 依赖 `domain`（实现其定义的仓储接口），不依赖 `app` 或 `api`
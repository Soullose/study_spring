```text
adapter/                         # 适配器层 - 外部接口
│   ├── web/                        # Web适配器
│   │   ├── controller/             # 原controller内容
│   │   └── converter/              # DTO转换器
│   ├── rpc/                        # RPC适配器
│   │   └── provider/               # 原interfaces.user内容
│   ├── mq/                         # 消息队列适配器
│   │   ├── consumer/
│   │   └── producer/
│   └── external/                   # 外部系统适配器
│       └── client/                 # 原client内容
```

### adapter（适配器层）
* 对应 DDD 层：接口层（Interface Layer）中的适配器部分  
* 职责：将外部请求（HTTP、RPC、消息等）转换为内部用例，并将内部结果转换为外部响应。包含：  
  * `web/`：Web 适配器（控制器、DTO 转换器）  
  * `rpc/`：RPC 适配器（提供者）  
  * `mq/`：消息队列适配器（消费者、生产者）  
  * `external/`：外部系统适配器（客户端）  
* 依赖关系：仅依赖 `api`（使用其声明的接口与 DTO），不依赖 `app` 或 `domain`，保证外部接口与内部实现的解耦。  
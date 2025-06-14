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


```text
app/                            # 应用层 - 业务流程编排
│   ├── service/                    # 应用服务
│   ├── command/                    # 原command内容
│   ├── query/                      # 查询服务
│   └── executor/                   # 命令执行器
```

### app（应用层）
* 对应 DDD 层：应用层（Application Layer）  
* 职责：协调领域对象完成具体的业务用例（Use Case）。包含：  
  * `service/`：应用服务（用例编排，如“创建用户并发送欢迎邮件”）  
  * `command/`：命令对象（CQRS 中的命令）  
  * `query/`：查询服务（CQRS 中的查询）  
  * `executor/`：命令执行器  
* 依赖关系：依赖 `api`（获取接口契约）和 `domain`（调用领域逻辑），不依赖 `infrastructure`（通过依赖注入获取仓储实现）。  
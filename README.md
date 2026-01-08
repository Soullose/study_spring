# study_spring

基于 springboot3.5.0 版本，用于学习，maven 多模块项目(主要包含 jpa,springSecurity,redis)

based on springboot3.5.0 version for learning , maven multi-module project ( mainly contains jpa, springSecurity, redis)



| 模块       | DDD 分层               | 主要职责                                                                 | 依赖关系说明                                                                 |
|------------|------------------------|--------------------------------------------------------------------------|------------------------------------------------------------------------------|
| `domain`   | 领域层（Domain Layer） | 核心业务逻辑、领域模型（实体、值对象、聚合根）、领域服务、仓储接口、领域事件 | 理想情况下应不依赖任何外部框架，但当前模块引入了 `spring-boot-starter-data-jpa`，导致领域模型与 JPA 耦合。建议剥离 JPA 依赖，仅保留纯领域对象。 |
| `app`      | 应用层（Application Layer） | 业务流程编排、用例执行（命令、查询）、应用服务、命令执行器               | 依赖 `api`（接口契约）和 `domain`（领域逻辑），符合“应用层协调领域对象完成用例”的原则。 |
| `infrastructure` | 基础设施层（Infrastructure Layer） | 技术实现：持久化（JPA 实体、Repository 实现）、外部服务集成、消息队列、缓存、配置、通用工具 | 依赖 `domain`，为领域层提供仓储、消息发送等技术实现。是 DDD 中“依赖倒置”的具体体现。 |
| `adapter`  | 接口层/适配器层（Interface Layer） | 外部接口适配：Web 控制器、RPC 提供者、消息队列消费者/生产者、外部系统客户端 | 仅依赖 `api`（接口契约），不直接依赖 `app` 或 `domain`，符合“适配器依赖接口而非实现”的原则。 |
| `api`      | 接口层/契约层（Interface Layer） | 声明对外暴露的接口契约、DTO（数据传输对象）、请求/响应模型               | 零依赖（仅声明），为 `adapter` 和 `app` 提供统一的接口规范，避免实现细节泄露到外部。 |



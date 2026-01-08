```text
领域层 - 核心业务逻辑
```

### domain（领域层）
* 对应 DDD 层：领域层（Domain Layer）  
* 职责：承载核心业务规则与状态变化。包含：  
  * `model/`：领域模型（实体、值对象、聚合根）  
  * `service/`：领域服务（处理跨实体的业务逻辑）  
  * `repository/`：仓储接口（定义数据访问契约）  
  * `event/`：领域事件（表示业务发生时触发的消息）  
* 当前问题：`pom.xml` 中引入了 `spring-boot-starter-data-jpa`，导致领域模型被 JPA 注解污染。建议：移除 JPA 依赖，将 JPA 实体移至 `infrastructure/persistence/entity`，并通过映射器与领域模型转换。  
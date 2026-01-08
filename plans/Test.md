domain/                              # 领域模块（纯业务逻辑）
├── src/main/java/com/wsf/domain/
│   ├── model/
│   │   ├── entity/                  # 领域实体（纯 Java 类，无 JPA 注解）
│   │   ├── valueobject/             # 值对象
│   │   └── aggregate/               # 聚合根
│   ├── repository/                  # 仓储接口（普通 Java 接口）
│   ├── service/                     # 领域服务
│   └── event/                       # 领域事件
└── pom.xml                          # 仅依赖必要的工具库（如 Lombok、MapStruct）

infrastructure/                      # 基础设施模块
├── src/main/java/com/wsf/infrastructure/
│   └── persistence/                 # 持久化子包
│       ├── entity/                  # JPA 实体（使用 @Entity 注解）
│       ├── repository/              # JPA Repository 接口（继承 JpaRepository）
│       ├── mapper/                  # 映射器（MapStruct），负责领域实体与 JPA 实体转换
│       └── impl/                    # 仓储接口实现类（实现 domain 层的 repository 接口）
└── pom.xml                          # 依赖 spring-boot-starter-data-jpa、mapstruct 等
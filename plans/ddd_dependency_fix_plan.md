# 解决app模块依赖infrastructure模块问题

## 一、问题分析

### 1.1 当前问题
- app模块依赖infrastructure模块是为了使用`CustomIdGenerator.generateId()`静态方法
- 这违反了DDD的依赖规则：app层不应该依赖infrastructure层

### 1.2 正确的依赖方向
```
api<--- app<--- domain
          |
          v
    infrastructure ---> domain
```

- **domain层**：核心层，不依赖任何其他层
- **app层**：依赖domain层和api层
- **infrastructure层**：依赖domain层，实现domain层定义的接口

## 二、解决方案

### 2.1 架构设计

在domain层定义ID生成器接口，在infrastructure层实现该接口：

```
domain/
└── src/main/java/com/wsf/domain/service/
    └── IdGenerator.java          # ID生成器接口

infrastructure/
└── src/main/java/com/wsf/infrastructure/id/
    └── UuidIdGenerator.java      # ID生成器实现（Spring Bean）
```

### 2.2 类设计

#### domain层 - IdGenerator接口
```java
package com.wsf.domain.service;

/**
 * ID生成器接口
 * 领域服务接口，由基础设施层实现
 */
public interface IdGenerator {
    /**
     * 生成唯一ID
     * @return 唯一ID字符串
     */
    String generate();
}
```

#### infrastructure层 - UuidIdGenerator实现
```java
package com.wsf.infrastructure.id;

import com.wsf.domain.service.IdGenerator;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.time.Instant;
import com.github.f4b6a3.uuid.alt.GUID;

/**
 * UUID ID生成器实现
 * 使用GUID v7生成时间有序的UUID
 */
@Component
public class UuidIdGenerator implements IdGenerator {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    @Override
    public String generate() {
        GUID guid = GUID.v7(Instant.now(), SECURE_RANDOM);
        return guid.toString();
    }
}
```

### 2.3 修改app层服务

将静态调用改为依赖注入：

**修改前：**
```java
String userId = CustomIdGenerator.generateId();
```

**修改后：**
```java
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final IdGenerator idGenerator;
    
    public UserDto createUser(CreateUserRequest request) {
        String userId = idGenerator.generate();
        // ...
    }
}
```

## 三、实施步骤

### 步骤1：在domain层创建IdGenerator接口
- 文件：`domain/src/main/java/com/wsf/domain/service/IdGenerator.java`
- 内容：定义`generate()`方法

### 步骤2：在infrastructure层创建UuidIdGenerator实现
- 文件：`infrastructure/src/main/java/com/wsf/infrastructure/id/UuidIdGenerator.java`
- 添加`@Component`注解，使其成为Spring Bean
- 实现domain层的IdGenerator接口

### 步骤3：修改app层服务实现类
- 修改`UserServiceImpl`、`RoleServiceImpl`、`MenuServiceImpl`、`DataPermissionServiceImpl`
- 将`CustomIdGenerator.generateId()`改为注入`IdGenerator`并调用`generate()`

### 步骤4：修改app模块pom.xml
- 移除对infrastructure模块的依赖
- 确保只依赖domain和api模块

### 步骤5：验证编译
- 运行`mvn clean compile -DskipTests`确保编译通过

## 四、影响分析

### 4.1 优点
- 遵循DDD依赖规则
- app层不再直接依赖infrastructure层
- 通过依赖注入，便于测试时替换实现

### 4.2 注意事项
- 需要确保Spring能正确扫描到infrastructure层的`UuidIdGenerator`组件
- 原来的`CustomIdGenerator`保留用于Hibernate JPA实体ID生成

## 五、文件变更清单

| 操作 | 文件路径 |
|------|----------|
| 新增 | domain/src/main/java/com/wsf/domain/service/IdGenerator.java |
| 新增 | infrastructure/src/main/java/com/wsf/infrastructure/id/UuidIdGenerator.java |
| 修改 | app/src/main/java/com/wsf/app/service/impl/UserServiceImpl.java |
| 修改 | app/src/main/java/com/wsf/app/service/impl/RoleServiceImpl.java |
| 修改 | app/src/main/java/com/wsf/app/service/impl/MenuServiceImpl.java |
| 修改 | app/src/main/java/com/wsf/app/service/impl/DataPermissionServiceImpl.java |
| 修改 | app/pom.xml |

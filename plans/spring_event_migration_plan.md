# Spring Event 迁移方案

## 1. 现状分析

### 1.1 现有组件清单

```
domain/event/          ← 旧事件包（5个事件类）
  ├── Event.java                领域事件接口
  ├── BaseEvent.java            基础事件抽象类
  ├── UserCreatedEvent.java     用户创建事件
  ├── AccountCreatedEvent.java  账户创建事件
  ├── AccountLockedEvent.java   账户锁定事件
  ├── RoleAssignedEvent.java    角色分配事件
  └── UserUpdatedEvent.java     用户更新事件

domain/events/         ← 新事件包（3个事件类，简化版）
  ├── Event.java                事件接口（与上面不同）
  ├── BaseEvent.java            基础事件抽象类
  └── UserCreatedEvent.java     用户创建事件（简化版）

infrastructure/eventbus/          ← 自定义事件总线（实际被使用的是这组）
  ├── EventBus.java              同步事件总线核心
  ├── EventHandler.java          事件处理器接口
  ├── EventHandlerScanner.java   BeanPostProcessor 扫描器
  ├── EventHandlerWrapper.java   SpEL 条件包装器
  ├── annotation/EventSubscribe.java  订阅注解
  └── README.md                  使用文档

infrastructure/config/
  └── EventBusConfiguration.java 注册 EventBus Bean

system/service/
  └── UserEventHandler.java      4个 @EventSubscribe 处理方法

rest/controller/
  └── EventBusDemoController.java  演示控制器（3个发布端点+1个状态端点）
```

### 1.2 关键发现

- `infrastructure/eventbus/` 只引用了 `domain/events/` 包（`com.wsf.domain.events.Event`），从未引用 `domain/event/`
- 但实际注册的处理器 `UserEventHandler` 也使用 `domain/events/UserCreatedEvent`
- `domain/event/` 和 `domain/events/` 是两套独立的事件体系，功能重复
- 自定义 EventBus 是**同步**的，不支持异步

---

## 2. 问题解答

### Q1: Spring Event 可以分组吗？

Spring 原生没有"分组 (Group)"概念，但有 **4 种等效方案**：

| 方案 | 优先级 | 实现方式 | 适用场景 |
|------|--------|----------|----------|
| 类型继承分组 | P0 | 事件按领域继承体系组织，`UserDomainEvent` extends `BaseDomainEvent`，监听时按父类统一接收 | 按业务领域分组（用户、订单、系统） |
| `@EventListener` + `condition` SpEL | P1 | 等同于自定义 `@EventSubscribe(condition=...)` | 按事件属性条件过滤 |
| `@Async` + 独立线程池 | P0 | 不同领域事件投递到不同线程池实现物理隔离 | 优先级隔离、资源隔离 |
| `ResolvableTypeProvider` 泛型事件 | P2 | Spring 4.2+ 精确匹配泛型事件类型 | 复杂泛型场景 |

**推荐策略**：类型继承 + `@Async` 线程池隔离，实现按领域分域消费。

### Q2: 100 个事件会有性能问题吗？

**不会。** 原因：

- Spring `SimpleApplicationEventMulticaster` 内部使用 `ConcurrentHashMap<ListenerCacheKey, List<ApplicationListener>>` 按事件类型 O(1) 查找
- 100 种事件类型 = map 中 100 个 key，查找开销可忽略不计
- **真正的瓶颈**：默认同步串行执行，若一个监听器耗时 500ms 会阻塞后续所有监听器
- **解决方案**：`@Async` + 专用线程池，所有监听器并行执行

**对比自定义 EventBus vs Spring Event：**

| 维度 | 自定义 EventBus | Spring Event |
|------|-----------------|-------------|
| 数据结构 | ConcurrentHashMap + CopyOnWriteArrayList（写时复制开销） | ConcurrentHashMap + 预计算快照 |
| 异步支持 | 无，需手动改造 | 开箱即用 `@Async` |
| 事务集成 | 无 | `@TransactionalEventListener`（支持事务后处理） |
| 生态集成 | 孤立的 | Spring 全栈（Metrics、Actuator、Sleuth 追踪） |
| 可观测性 | 自建 | 与 Micrometer 集成，自动采集指标 |

---

## 3. 迁移方案

### 3.1 整体架构变更

```mermaid
graph TB
    subgraph "迁移前"
        E1[domain/event/ Event 接口] --> BE1[BaseEvent]
        BE1 --> UE1[UserCreatedEvent]
        BE1 --> AE1[AccountCreatedEvent]
        BE1 --> ALE1[AccountLockedEvent]
        BE1 --> RAE1[RoleAssignedEvent]
        BE1 --> UUE1[UserUpdatedEvent]
        
        E2[domain/events/ Event 接口] --> BE2[BaseEvent]
        BE2 --> UE2[UserCreatedEvent]
        
        EB[自定义 EventBus] --> E2
        EB --> EV[EventHandlerScanner]
        EV --> @ES[@EventSubscribe 注解]
        
        UH[UserEventHandler] --> @ES
        DC[EventBusDemoController] --> EB
    end

    subgraph "迁移后"
        BDE[BaseDomainEvent extends ApplicationEvent]
        BDE --> UDE[UserDomainEventType 标记接口]
        
        UDE --> UCE[UserCreatedEvent]
        UDE --> UUE2[UserUpdatedEvent]
        
        BDE --> ADE[AccountDomainEventType 标记接口]
        ADE --> ACE[AccountCreatedEvent]
        ADE --> ALE2[AccountLockedEvent]
        
        BDE --> RE2[RoleAssignedEvent]
        
        AEP[ApplicationEventPublisher] --> BDE
        @EL[@EventListener + @Async] --> BDE
        
        UH2[UserEventHandler] --> @EL
        DCN[EventBusDemoController] --> AEP
        
        TP1[threadPool: event-user] --> @EL
        TP2[threadPool: event-account] --> @EL
        TP3[threadPool: event-system] --> @EL
    end
```

### 3.2 注解映射表

| 自定义 `@EventSubscribe` | Spring `@EventListener` | 说明 |
|--------------------------|------------------------|------|
| `@EventSubscribe` | `@EventListener` | 方法注解，标记事件监听器 |
| `order = 1` | `@Order(1)` | 执行优先级 |
| `condition = "#event.userId > 100"` | `condition = "#event.userId > 100"` | SpEL 条件（语法完全兼容） |
| 无（同步执行） | `@Async` | 异步执行 |
| `value = SomeEvent.class` | 方法参数类型自动推断 | 事件类型匹配 |

### 3.3 分组/分域方案

通过 **事件标记接口 + 独立线程池** 实现按领域分组：

```java
// 领域标记接口（无方法，仅用于分组）
public interface UserDomainEventType {}
public interface AccountDomainEventType {}

// 用户域事件
public class UserCreatedEvent extends BaseDomainEvent implements UserDomainEventType { ... }

// 配置：不同领域 → 不同线程池
@Configuration
@EnableAsync
public class EventAsyncConfig {
    
    @Bean("eventUserExecutor")
    public Executor eventUserExecutor() {
        // 专用于用户域事件
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-user-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        return executor;
    }
    
    @Bean("eventAccountExecutor")
    public Executor eventAccountExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("event-account-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        return executor;
    }

    @Bean("eventSystemExecutor")
    public Executor eventSystemExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("event-system-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        return executor;
    }
}

// 使用：不同领域的监听器指定不同线程池
@Component
public class UserEventHandler {
    @Async("eventUserExecutor")
    @EventListener
    @Order(1)
    public void sendWelcomeEmail(UserCreatedEvent event) { ... }
}

@Component
public class AccountEventHandler {
    @Async("eventAccountExecutor")
    @EventListener
    public void onAccountCreated(AccountCreatedEvent event) { ... }
}
```

### 3.4 目标目录结构

```
domain/
  └── event/
      ├── BaseDomainEvent.java              ← 统一事件基类（extends ApplicationEvent）
      ├── UserDomainEventType.java           ← 用户域标记接口
      ├── AccountDomainEventType.java        ← 账户域标记接口
      ├── UserCreatedEvent.java
      ├── UserUpdatedEvent.java
      ├── AccountCreatedEvent.java
      ├── AccountLockedEvent.java
      └── RoleAssignedEvent.java

infrastructure/
  └── config/
      ├── EventAsyncConfiguration.java       ← 异步事件 + 线程池配置（代替 EventBusConfiguration）
      └── SpringEventConfiguration.java      ← 可选：自定义事件广播器（错误处理增强）

删除以下文件：
  ✗ infrastructure/eventbus/ 整个目录（6个文件）
  ✗ infrastructure/config/EventBusConfiguration.java
  ✗ domain/events/ 整个目录
  ✗ system/enums/EnumEvents.java（未被事件体系使用）
  ✗ plans/api_app_module_plan.md 等旧计划文件（可选清理）
```

---

## 4. 详细变更清单

### 4.1 新建文件（3个）

| 文件 | 说明 |
|------|------|
| `domain/.../event/BaseDomainEvent.java` | 统一基类，继承 `ApplicationEvent`，提供 `eventId`、`timestamp`、`source` |
| `domain/.../event/UserDomainEventType.java` | 用户域标记接口 |
| `domain/.../event/AccountDomainEventType.java` | 账户域标记接口 |
| `infrastructure/.../config/SpringEventConfiguration.java` | 异步 + 线程池 + 自定义广播器配置 |

### 4.2 修改文件（7个）

| 文件 | 变更 |
|------|------|
| `domain/.../event/UserCreatedEvent.java` | 改为 `extends BaseDomainEvent implements UserDomainEventType` |
| `domain/.../event/UserUpdatedEvent.java` | 同上 |
| `domain/.../event/AccountCreatedEvent.java` | 改为 `extends BaseDomainEvent implements AccountDomainEventType` |
| `domain/.../event/AccountLockedEvent.java` | 同上 |
| `domain/.../event/RoleAssignedEvent.java` | 改为 `extends BaseDomainEvent`（系统域，不实现特定标记接口） |
| `system/.../service/UserEventHandler.java` | `@EventSubscribe` → `@EventListener`，加 `@Async("eventUserExecutor")` |
| `rest/.../controller/EventBusDemoController.java` | `EventBus.publish()` → `ApplicationEventPublisher.publishEvent()` |

### 4.3 删除内容

- 整个 `infrastructure/eventbus/` 包（6 个文件）
- `infrastructure/config/EventBusConfiguration.java`
- `domain/events/` 整个包（3 个文件）
- `domain/event/Event.java` 和 `domain/event/BaseEvent.java`（由 `BaseDomainEvent` 替代）

---

## 5. 事务感知增强

Spring 提供 `@TransactionalEventListener`，配合 `TransactionPhase` 枚举实现事务后处理：

```java
@Async("eventUserExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void sendWelcomeEmail(UserCreatedEvent event) { ... }

// 可选阶段：
// BEFORE_COMMIT  事务提交前
// AFTER_COMMIT   事务提交后（默认，推荐）
// AFTER_ROLLBACK 事务回滚后
// AFTER_COMPLETION 事务完成（提交或回滚）
```

这是自定义 EventBus 不具备的能力，强烈推荐使用。

---

## 6. 执行步骤（按顺序）

| 步骤 | 操作 | 优先级 |
|------|------|--------|
| 1 | 创建 `BaseDomainEvent` 基类（继承 `ApplicationEvent`） | P0 |
| 2 | 创建领域标记接口 `UserDomainEventType`、`AccountDomainEventType` | P0 |
| 3 | 改造 `domain/event/` 下 5 个事件类继承 `BaseDomainEvent` | P0 |
| 4 | 创建 `SpringEventConfiguration` 异步配置 | P0 |
| 5 | 改造 `UserEventHandler`：`@EventSubscribe` → `@EventListener` + `@Async` | P0 |
| 6 | 改造 `EventBusDemoController`：`EventBus.publish()` → `ApplicationEventPublisher.publishEvent()` | P0 |
| 7 | 删除 `infrastructure/eventbus/`、`EventBusConfiguration`、`domain/events/`、旧接口 | P0 |
| 8 | 确认 `pom.xml` 无需新增依赖（Spring Event 随 spring-context 已包含） | P1 |
| 9 | 编写 `SpringEventTests` 验证异步事件发布/订阅 | P1 |

---

## 7. 风险与缓解

| 风险 | 缓解措施 |
|------|----------|
| 异步事件中事务上下文丢失 | 使用 `@TransactionalEventListener` 在事务提交后处理 |
| 异步监听器中 `SecurityContext` 丢失 | 配置 `SecurityContextHolder.setStrategyName(MODE_INHERITABLETHREADLOCAL)` |
| `@Async` 线程池耗尽 | 配置 `CallerRunsPolicy` 拒绝策略，配合监控告警 |
| 事件丢失（异步无持久化） | 对关键事件配合 Redis/MQ 做事件持久化和重试 |
| SpEL 条件表达式中引用 Spring Bean 语法变化 | `@beanName` 语法在 `@EventListener` 的 condition 中仍然有效 |

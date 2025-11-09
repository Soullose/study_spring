# 事件总线 (EventBus) 使用文档

## 概述

这是一个轻量级的同步事件总线实现，不依赖 Spring 的 `ApplicationEventPublisher`，但能与 Spring 集成管理。支持基于注解的事件订阅和条件过滤。

## 核心特性

- ✅ **轻量级同步处理** - 事件发布后立即同步执行所有处理器
- ✅ **类型安全** - 基于泛型的事件类型匹配
- ✅ **Spring 集成** - 自动扫描和注册事件处理器
- ✅ **模块解耦** - 支持跨模块事件通信
- ✅ **线程安全** - 使用并发安全的集合类
- ✅ **条件过滤** - 基于 SpEL 表达式的条件执行
- ✅ **处理顺序** - 支持处理器执行优先级控制

## 快速开始

### 1. 定义事件

```java
// 继承 BaseEvent 创建自定义事件
public class UserCreatedEvent extends BaseEvent {
    private final Long userId;
    private final String username;

    public UserCreatedEvent(Object source, Long userId, String username) {
        super(source);
        this.userId = userId;
        this.username = username;
    }

    // getters...
}
```

### 2. 创建事件处理器

```java
@Component
public class UserEventHandler {

    // 基础事件处理
    @EventSubscribe
    public void handleUserCreated(UserCreatedEvent event) {
        System.out.println("处理用户创建事件: " + event.getUsername());
    }

    // 带优先级的事件处理
    @EventSubscribe(order = 1)
    public void sendWelcomeEmail(UserCreatedEvent event) {
        System.out.println("发送欢迎邮件给: " + event.getUsername());
    }

    // 带条件过滤的事件处理
    @EventSubscribe(condition = "#event.userId > 100")
    public void logUserCreation(UserCreatedEvent event) {
        System.out.println("记录用户创建日志: " + event.getUserId());
    }

    // 复杂条件过滤
    @EventSubscribe(condition = "#event.username.contains('vip')")
    public void handleVipUser(UserCreatedEvent event) {
        System.out.println("VIP用户特殊处理: " + event.getUsername());
    }
}
```

### 3. 发布事件

```java
@RestController
public class UserController {

    private final EventBus eventBus;

    public UserController(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostMapping("/users")
    public String createUser(@RequestBody User user) {
        // 业务逻辑...
        
        // 发布用户创建事件
        UserCreatedEvent event = new UserCreatedEvent(this, user.getId(), user.getUsername());
        eventBus.publish(event);
        
        return "用户创建成功";
    }
}
```

## 注解说明

### @EventSubscribe

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `Class<?>` | `void.class` | 指定事件类型，默认从方法参数推断 |
| `order` | `int` | `0` | 处理顺序，数值越小优先级越高 |
| `condition` | `String` | `""` | SpEL 条件表达式，结果为 true 时才执行 |

### SpEL 表达式示例

```java
// 简单属性判断
@EventSubscribe(condition = "#event.userId > 100")

// 字符串操作
@EventSubscribe(condition = "#event.username.contains('admin')")

// 多条件组合
@EventSubscribe(condition = "#event.userId > 100 && #event.username.startsWith('user')")

// 调用方法
@EventSubscribe(condition = "#event.isValid()")

// 访问 Spring Bean (需要 Bean 在上下文中)
@EventSubscribe(condition = "@userService.isUserActive(#event.userId)")
```

## 配置说明

事件总线会自动配置，无需额外配置。如果需要自定义，可以创建配置类：

```java
@Configuration
public class CustomEventBusConfig {

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean
    public EventHandlerScanner eventHandlerScanner(EventBus eventBus, ApplicationContext context) {
        EventHandlerScanner scanner = new EventHandlerScanner(eventBus);
        // 设置 BeanFactoryResolver 以便在 SpEL 中访问 Spring Bean
        eventBus.setBeanFactoryResolver(new BeanFactoryResolver(context));
        return scanner;
    }
}
```

## 最佳实践

### 1. 事件设计原则

- **单一职责**: 每个事件应该只代表一个业务动作
- **不可变性**: 事件应该是不可变的对象
- **自描述性**: 事件名称应该清晰表达发生了什么

### 2. 处理器设计原则

- **无状态**: 处理器应该尽可能无状态
- **快速执行**: 避免在处理器中执行耗时操作
- **异常处理**: 妥善处理异常，避免影响其他处理器

### 3. 条件过滤使用场景

- **环境过滤**: 根据运行环境决定是否执行
- **权限过滤**: 根据用户权限决定是否执行
- **业务状态过滤**: 根据业务状态决定是否执行
- **数据范围过滤**: 根据数据范围决定是否执行

## 测试示例

### 单元测试

```java
@SpringBootTest
class EventBusTest {

    @Autowired
    private EventBus eventBus;

    @Test
    void testEventPublishing() {
        UserCreatedEvent event = new UserCreatedEvent(this, 123L, "testuser");
        
        // 发布事件
        eventBus.publish(event);
        
        // 验证事件处理逻辑
        // ...
    }
}
```

### API 测试

启动应用后，可以通过以下 API 测试事件总线：

```
GET /api/eventbus/status                          # 查看事件总线状态
GET /api/eventbus/publish-user-created?userId=101&username=test&email=test@example.com
GET /api/eventbus/publish-vip-user?userId=102     # 测试VIP用户
GET /api/eventbus/publish-small-user?userId=50    # 测试条件过滤
```

## 故障排除

### 1. 事件处理器未执行

- 检查处理器类是否被 Spring 扫描 (`@Component`)
- 检查方法是否有 `@EventSubscribe` 注解
- 检查方法参数类型是否正确
- 检查条件表达式是否正确

### 2. SpEL 表达式不工作

- 确保表达式语法正确
- 使用 `#event` 引用事件对象
- 访问 Spring Bean 使用 `@beanName`

### 3. 性能问题

- 避免在处理器中执行耗时操作
- 考虑使用异步处理（当前版本为同步）
- 合理使用条件过滤减少不必要的处理

## 扩展建议

### 异步处理
如果需要异步处理，可以扩展 `EventBus` 类，添加线程池支持。

### 事件持久化
可以添加事件持久化机制，支持事件重放。

### 监控和指标
可以添加事件处理监控和性能指标收集。

## 版本说明

- **v1.0**: 基础同步事件总线，支持注解订阅和条件过滤
- **计划特性**: 异步处理、事件持久化、监控指标

---

如有问题或建议，请提交 Issue 或 Pull Request。
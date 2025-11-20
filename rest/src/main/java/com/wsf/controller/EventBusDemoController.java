package com.wsf.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsf.domain.events.UserCreatedEvent;
import com.wsf.infrastructure.eventbus.EventBus;

/**
 * 事件总线演示控制器
 * 用于测试事件总线的功能
 */
@RestController
@RequestMapping("/api/eventbus")
public class EventBusDemoController {

    private static final Logger log = LoggerFactory.getLogger(EventBusDemoController.class);

    private final EventBus eventBus;

    public EventBusDemoController(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * 发布用户创建事件
     * @param userId 用户ID
     * @param username 用户名
     * @param email 邮箱
     * @return 操作结果
     */
    @GetMapping("/publish-user-created")
    public String publishUserCreated(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam String email) {
        
        log.info("发布用户创建事件 - 用户ID: {}, 用户名: {}, 邮箱: {}", userId, username, email);
        
        UserCreatedEvent event = new UserCreatedEvent(this, userId, username, email);
        eventBus.publish(event);
        
        return String.format("用户创建事件发布成功 - 用户ID: %d, 用户名: %s", userId, username);
    }

    /**
     * 测试VIP用户事件
     * @param userId 用户ID
     * @return 操作结果
     */
    @GetMapping("/publish-vip-user")
    public String publishVipUser(@RequestParam Long userId) {
        String username = "vip_user_" + userId;
        String email = "vip" + userId + "@example.com";
        
        log.info("发布VIP用户创建事件 - 用户ID: {}, 用户名: {}", userId, username);
        
        UserCreatedEvent event = new UserCreatedEvent(this, userId, username, email);
        eventBus.publish(event);
        
        return String.format("VIP用户创建事件发布成功 - 用户ID: %d, 用户名: %s", userId, username);
    }

    /**
     * 测试条件过滤事件
     * @param userId 用户ID（测试小于100的用户不会被记录日志）
     * @return 操作结果
     */
    @GetMapping("/publish-small-user")
    public String publishSmallUser(@RequestParam Long userId) {
        String username = "user_" + userId;
        String email = "user" + userId + "@example.com";
        
        log.info("发布小用户ID事件 - 用户ID: {}, 用户名: {}", userId, username);
        
        UserCreatedEvent event = new UserCreatedEvent(this, userId, username, email);
        eventBus.publish(event);
        
        return String.format("小用户ID事件发布成功 - 用户ID: %d, 用户名: %s", userId, username);
    }

    /**
     * 获取事件总线状态
     * @return 事件总线状态信息
     */
    @GetMapping("/status")
    public String getEventBusStatus() {
        StringBuilder status = new StringBuilder();
        status.append("事件总线状态:\n");
        status.append("已注册的事件类型: ").append(eventBus.getRegisteredEventTypes().size()).append("\n");
        
//        for (Class<?> eventType : eventBus.getRegisteredEventTypes()) {
//            int handlerCount = eventBus.getHandlerCount(eventType);
//            status.append("  - ").append(eventType.getSimpleName())
//                  .append(": ").append(handlerCount).append(" 个处理器\n");
//        }
        
        return status.toString();
    }
}
package com.wsf.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsf.domain.event.UserCreatedEvent;
import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.UserName;

/**
 * Spring Event 演示控制器（代替原 EventBusDemoController）。
 * <p>
 * 使用 Spring 标准的 {@link ApplicationEventPublisher} 发布领域事件，
 * 由 {@code @EventListener} 订阅方异步处理。
 * </p>
 *
 * @author wsf
 */
@RestController
@RequestMapping("/api/event")
public class EventBusDemoController {

    private static final Logger log = LoggerFactory.getLogger(EventBusDemoController.class);

    private final ApplicationEventPublisher eventPublisher;

    public EventBusDemoController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 发布用户创建事件。
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param email    邮箱
     * @return 操作结果
     */
    @GetMapping("/publish-user-created")
    public String publishUserCreated(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam String email) {

        log.info("发布用户创建事件 - 用户ID: {}, 用户名: {}, 邮箱: {}", userId, username, email);

        // UserName 是 record(firstName, lastName)，这里把入参用户名拆分为 firstName
        UserName userName = new UserName(username, "");
        Email userEmail = new Email(email);

        UserCreatedEvent event = new UserCreatedEvent(this, userId.toString(), userName, userEmail);
        eventPublisher.publishEvent(event);

        return String.format("用户创建事件发布成功 - 用户ID: %d, 用户名: %s", userId, username);
    }

    /**
     * 测试VIP用户事件。
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @GetMapping("/publish-vip-user")
    public String publishVipUser(@RequestParam Long userId) {
        String username = "vip_user_" + userId;
        String email = "vip" + userId + "@example.com";

        log.info("发布VIP用户创建事件 - 用户ID: {}, 用户名: {}", userId, username);

        UserName userName = new UserName(username, "");
        Email userEmail = new Email(email);

        UserCreatedEvent event = new UserCreatedEvent(this, userId.toString(), userName, userEmail);
        eventPublisher.publishEvent(event);

        return String.format("VIP用户创建事件发布成功 - 用户ID: %d, 用户名: %s", userId, username);
    }

    /**
     * 测试条件过滤事件。
     *
     * @param userId 用户ID（测试小于100的用户不会被记录日志）
     * @return 操作结果
     */
    @GetMapping("/publish-small-user")
    public String publishSmallUser(@RequestParam Long userId) {
        String username = "user_" + userId;
        String email = "user" + userId + "@example.com";

        log.info("发布小用户ID事件 - 用户ID: {}, 用户名: {}", userId, username);

        UserName userName = new UserName(username, "");
        Email userEmail = new Email(email);

        UserCreatedEvent event = new UserCreatedEvent(this, userId.toString(), userName, userEmail);
        eventPublisher.publishEvent(event);

        return String.format("小用户ID事件发布成功 - 用户ID: %d, 用户名: %s", userId, username);
    }

    /**
     * 获取事件状态信息。
     *
     * @return 事件系统状态
     */
    @GetMapping("/status")
    public String getEventStatus() {
        return """
                事件系统状态:
                - 事件引擎: Spring ApplicationEvent
                - 发布方式: ApplicationEventPublisher.publishEvent()
                - 订阅方式: @EventListener + @Async
                - 用户域线程池: eventUserExecutor
                - 账户域线程池: eventAccountExecutor
                - 系统域线程池: eventSystemExecutor
                - 条件过滤: SpEL condition 表达式
                - 优先级控制: @Order 注解
                """;
    }
}

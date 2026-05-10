package com.wsf.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.wsf.domain.event.UserCreatedEvent;

/**
 * 用户事件处理器（Spring Event 版）。
 * <p>
 * 演示如何使用 {@code @EventListener} + {@code @Async} + {@code @Order} 组合
 * 替代原有的 {@code @EventSubscribe} 自定义注解。
 * </p>
 * <ul>
 *   <li>{@code @EventListener} —— 订阅 {@link UserCreatedEvent} 事件</li>
 *   <li>{@code @Order(1)} —— 执行优先级（数值越小越先执行，替代原 order 属性）</li>
 *   <li>{@code @Async("eventUserExecutor")} —— 使用用户域专用线程池异步执行</li>
 *   <li>{@code condition} —— SpEL 条件过滤（语法与 @EventSubscribe 完全兼容）</li>
 * </ul>
 *
 * @author wsf
 */
@Component
public class UserEventHandler {

    private static final Logger log = LoggerFactory.getLogger(UserEventHandler.class);

    /**
     * 处理用户创建事件 —— 发送欢迎邮件。
     * 优先级最高，最先执行。
     */
    @Async("eventUserExecutor")
    @EventListener
    @Order(1)
    public void sendWelcomeEmail(UserCreatedEvent event) {
        log.info("[user-event] 发送欢迎邮件给用户: {}, 邮箱: {}", event.getName(), event.getEmail());
        simulateWork(100);
        log.info("[user-event] 欢迎邮件发送成功 - 用户ID: {}", event.getUserId());
    }

    /**
     * 处理用户创建事件 —— 初始化用户资料。
     * 优先级中等。
     */
    @Async("eventUserExecutor")
    @EventListener
    @Order(2)
    public void initializeUserProfile(UserCreatedEvent event) {
        log.info("[user-event] 初始化用户资料 - 用户ID: {}, 用户名: {}", event.getUserId(), event.getName());
        simulateWork(50);
        log.info("[user-event] 用户资料初始化完成 - 用户ID: {}", event.getUserId());
    }

    /**
     * 处理用户创建事件 —— 记录用户创建日志。
     * 优先级较低，最后执行。
     * 条件过滤：只处理用户ID大于100的事件。
     */
    @Async("eventUserExecutor")
    @EventListener(condition = "#event.userId > 100")
    @Order(3)
    public void logUserCreation(UserCreatedEvent event) {
        log.info("[user-event] 记录用户创建日志 - 用户ID: {}, 用户名: {}", event.getUserId(), event.getName());
        log.info("[user-event] 用户创建日志记录完成 - 用户ID: {}", event.getUserId());
    }

    /**
     * 处理用户创建事件 —— VIP用户特殊处理。
     * 条件过滤：只处理VIP用户（用户名包含vip）。
     */
    @Async("eventUserExecutor")
    @EventListener(condition = "#event.name.value.contains('vip')")
    public void handleVipUser(UserCreatedEvent event) {
        log.info("[user-event] VIP用户特殊处理 - 用户ID: {}, 用户名: {}", event.getUserId(), event.getName());
        log.info("[user-event] VIP用户特权已激活 - 用户ID: {}", event.getUserId());
    }

    /**
     * 模拟耗时操作。
     */
    private void simulateWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

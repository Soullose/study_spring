package com.wsf.service;

import com.wsf.domain.events.UserCreatedEvent;
import com.wsf.infrastructure.eventbus.annotation.EventSubscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 用户事件处理器示例
 * 演示如何使用事件总线处理用户相关事件
 */
@Component
public class UserEventHandler {

    private static final Logger log = LoggerFactory.getLogger(UserEventHandler.class);

    /**
     * 处理用户创建事件 - 发送欢迎邮件
     * 优先级较高，最先执行
     */
    @EventSubscribe(order = 1)
    public void sendWelcomeEmail(UserCreatedEvent event) {
        log.info("发送欢迎邮件给用户: {}, 邮箱: {}", event.getUsername(), event.getEmail());
        // 模拟发送邮件逻辑
        try {
            Thread.sleep(100);
            log.info("欢迎邮件发送成功 - 用户ID: {}", event.getUserId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理用户创建事件 - 初始化用户资料
     * 优先级中等
     */
    @EventSubscribe(order = 2)
    public void initializeUserProfile(UserCreatedEvent event) {
        log.info("初始化用户资料 - 用户ID: {}, 用户名: {}", event.getUserId(), event.getUsername());
        // 模拟初始化逻辑
        try {
            Thread.sleep(50);
            log.info("用户资料初始化完成 - 用户ID: {}", event.getUserId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理用户创建事件 - 记录用户创建日志
     * 优先级较低，最后执行
     * 条件过滤：只处理用户ID大于100的事件
     */
    @EventSubscribe(order = 3, condition = "#event.userId > 100")
    public void logUserCreation(UserCreatedEvent event) {
        log.info("记录用户创建日志 - 用户ID: {}, 用户名: {}", event.getUserId(), event.getUsername());
        // 模拟日志记录逻辑
        log.info("用户创建日志记录完成 - 用户ID: {}", event.getUserId());
    }

    /**
     * 处理用户创建事件 - VIP用户特殊处理
     * 条件过滤：只处理VIP用户（用户名包含vip）
     */
    @EventSubscribe(condition = "#event.username.contains('vip')")
    public void handleVipUser(UserCreatedEvent event) {
        log.info("VIP用户特殊处理 - 用户ID: {}, 用户名: {}", event.getUserId(), event.getUsername());
        // 模拟VIP用户特殊逻辑
        log.info("VIP用户特权已激活 - 用户ID: {}", event.getUserId());
    }
}
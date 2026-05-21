package com.wsf.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.UserName;

/**
 * Spring Event 迁移验证集成测试。
 * <p>
 * 验证 Spring 标准 {@code ApplicationEventPublisher} + {@code @EventListener} + {@code @Async} 完整链路。
 * </p>
 *
 * @author wsf
 */
@org.junit.jupiter.api.Disabled("需要完整数据库和Redis环境，跳过集成测试")
@SpringBootTest(classes = {
        com.wsf.StartApplication.class,
        SpringEventMigrationTest.TestConfig.class
})
@Import({
        SpringEventMigrationTest.TestEventCollector.class,
        SpringEventMigrationTest.OrderedTestListener.class,
        SpringEventMigrationTest.ConditionalTestListener.class
})
@DisplayName("Spring Event 迁移验证集成测试")
class SpringEventMigrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TestEventCollector collector;

    @BeforeEach
    void setUp() {
        collector.reset();
    }

    @Test
    @DisplayName("should_receiveUserCreatedEvent_when_published")
    void should_receiveUserCreatedEvent_when_published() throws Exception {
        UserName userName = new UserName("test", "user");
        Email email = new Email("test@example.com");
        UserCreatedEvent event = new UserCreatedEvent(this, "1001", userName, email);

        eventPublisher.publishEvent(event);

        assertTrue(collector.userCreatedLatch.await(5, TimeUnit.SECONDS),
                "应在 5 秒内异步接收到 UserCreatedEvent");
        assertEquals(1, collector.userCreatedEvents.size());
        assertEquals("1001", collector.userCreatedEvents.peek().getUserId());
    }

    @Test
    @DisplayName("should_receiveAccountCreatedEvent_when_published")
    void should_receiveAccountCreatedEvent_when_published() throws Exception {
        AccountCreatedEvent event = new AccountCreatedEvent(this, "ACC-001", "john", "1001");

        eventPublisher.publishEvent(event);

        assertTrue(collector.accountCreatedLatch.await(5, TimeUnit.SECONDS));
        assertEquals(1, collector.accountCreatedEvents.size());
        assertEquals("ACC-001", collector.accountCreatedEvents.peek().getAccountId());
    }

    @Test
    @DisplayName("should_receiveRoleAssignedEvent_when_published")
    void should_receiveRoleAssignedEvent_when_published() throws Exception {
        RoleAssignedEvent event = new RoleAssignedEvent(
                this, "ACC-001", Set.of("ROLE-ADMIN"), Set.of("ADMIN"));

        eventPublisher.publishEvent(event);

        assertTrue(collector.roleAssignedLatch.await(5, TimeUnit.SECONDS));
        assertEquals(1, collector.roleAssignedEvents.size());
    }

    @Test
    @DisplayName("should_execute_handlers_in_order")
    void should_execute_handlers_in_order() throws Exception {
        collector.orderedResults.clear();

        UserName userName = new UserName("order", "test");
        Email email = new Email("order@example.com");
        UserCreatedEvent event = new UserCreatedEvent(this, "2001", userName, email);

        eventPublisher.publishEvent(event);

        assertTrue(collector.orderedLatch.await(5, TimeUnit.SECONDS));
        assertEquals(2, collector.orderedResults.size(), "应有 2 个 OrderedListener 被调用");
        assertEquals("sendWelcomeEmail", collector.orderedResults.toArray()[0]);
        assertEquals("initializeUserProfile", collector.orderedResults.toArray()[1]);
    }

    @Test
    @DisplayName("should_skip_when_condition_not_met")
    void should_skip_when_condition_not_met() throws Exception {
        UserName userName = new UserName("small", "id");
        Email email = new Email("small@example.com");
        UserCreatedEvent event = new UserCreatedEvent(this, "50", userName, email);

        eventPublisher.publishEvent(event);

        boolean triggered = collector.conditionalLatch.await(2, TimeUnit.SECONDS);
        assertFalse(triggered, "userId=50 不满足 condition='#event.userId > 100'");
    }

    @Test
    @DisplayName("should_trigger_when_condition_met")
    void should_trigger_when_condition_met() throws Exception {
        UserName userName = new UserName("big", "id");
        Email email = new Email("big@example.com");
        UserCreatedEvent event = new UserCreatedEvent(this, "200", userName, email);

        eventPublisher.publishEvent(event);

        assertTrue(collector.conditionalLatch.await(5, TimeUnit.SECONDS),
                "userId=200 满足 condition，应触发");
    }

    @Test
    @DisplayName("should_listen_all_user_events_via_UserDomainEventType")
    void should_listen_all_user_events_via_UserDomainEventType() throws Exception {
        UserName userName = new UserName("domain", "test");
        Email email = new Email("domain@example.com");
        UserCreatedEvent created = new UserCreatedEvent(this, "3001", userName, email);
        UserUpdatedEvent updated = new UserUpdatedEvent(this, "3001", userName, email);

        eventPublisher.publishEvent(created);
        eventPublisher.publishEvent(updated);

        assertTrue(collector.userDomainLatch.await(5, TimeUnit.SECONDS));
        assertTrue(collector.userDomainEvents.size() >= 2,
                "通过 UserDomainEventType 应监听到 >=2 个用户域事件");
    }

    // ==================== 测试用内部组件 ====================

    @Component
    static class TestEventCollector {

        final CountDownLatch userCreatedLatch = new CountDownLatch(1);
        final CountDownLatch accountCreatedLatch = new CountDownLatch(1);
        final CountDownLatch roleAssignedLatch = new CountDownLatch(1);
        final CountDownLatch orderedLatch = new CountDownLatch(2);
        final CountDownLatch conditionalLatch = new CountDownLatch(1);
        final CountDownLatch userDomainLatch = new CountDownLatch(2);

        final ConcurrentLinkedQueue<UserCreatedEvent> userCreatedEvents = new ConcurrentLinkedQueue<>();
        final ConcurrentLinkedQueue<AccountCreatedEvent> accountCreatedEvents = new ConcurrentLinkedQueue<>();
        final ConcurrentLinkedQueue<RoleAssignedEvent> roleAssignedEvents = new ConcurrentLinkedQueue<>();
        final ConcurrentLinkedQueue<String> orderedResults = new ConcurrentLinkedQueue<>();
        final ConcurrentLinkedQueue<BaseDomainEvent> userDomainEvents = new ConcurrentLinkedQueue<>();

        void reset() {
            userCreatedEvents.clear();
            accountCreatedEvents.clear();
            roleAssignedEvents.clear();
            orderedResults.clear();
            userDomainEvents.clear();
        }

        @Async("eventUserExecutor")
        @EventListener
        public void onUserCreated(UserCreatedEvent e) {
            userCreatedEvents.add(e);
            userCreatedLatch.countDown();
        }

        @Async("eventAccountExecutor")
        @EventListener
        public void onAccountCreated(AccountCreatedEvent e) {
            accountCreatedEvents.add(e);
            accountCreatedLatch.countDown();
        }

        @Async("eventSystemExecutor")
        @EventListener
        public void onRoleAssigned(RoleAssignedEvent e) {
            roleAssignedEvents.add(e);
            roleAssignedLatch.countDown();
        }

        @Async("eventUserExecutor")
        @EventListener
        public void onUserDomainEvent(UserDomainEventType e) {
            if (e instanceof BaseDomainEvent bde) {
                userDomainEvents.add(bde);
                userDomainLatch.countDown();
            }
        }
    }

    @Component
    static class OrderedTestListener {

        private final TestEventCollector collector;

        OrderedTestListener(TestEventCollector collector) {
            this.collector = collector;
        }

        @Async("eventUserExecutor")
        @EventListener
        @Order(1)
        public void sendWelcomeEmail(UserCreatedEvent event) {
            collector.orderedResults.add("sendWelcomeEmail");
            collector.orderedLatch.countDown();
        }

        @Async("eventUserExecutor")
        @EventListener
        @Order(2)
        public void initializeUserProfile(UserCreatedEvent event) {
            collector.orderedResults.add("initializeUserProfile");
            collector.orderedLatch.countDown();
        }
    }

    @Component
    static class ConditionalTestListener {

        private final TestEventCollector collector;

        ConditionalTestListener(TestEventCollector collector) {
            this.collector = collector;
        }

        @Async("eventUserExecutor")
        @EventListener(condition = "#event.userId > 100")
        public void logUserCreation(UserCreatedEvent event) {
            collector.conditionalLatch.countDown();
        }
    }

    @Configuration
    @EnableAsync
    static class TestConfig {
    }
}

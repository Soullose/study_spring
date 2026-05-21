package com.wsf.domain.event;

import com.wsf.domain.model.user.valueobject.Email;
import com.wsf.domain.model.user.valueobject.UserName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("领域事件测试")
class BaseDomainEventTest {

    @Test
    @DisplayName("应生成唯一事件ID")
    void should_generateUniqueEventId() {
        UserName name = new UserName("test", "user");
        Email email = new Email("test@example.com");

        UserCreatedEvent e1 = new UserCreatedEvent(this, "1001", name, email);
        UserCreatedEvent e2 = new UserCreatedEvent(this, "1002", name, email);

        assertThat(e1.getEventId()).isNotNull();
        assertThat(e2.getEventId()).isNotNull();
        assertThat(e1.getEventId()).isNotEqualTo(e2.getEventId());
    }

    @Test
    @DisplayName("应记录事件时间戳")
    void should_recordTimestamp() {
        UserName name = new UserName("test", "user");
        Email email = new Email("test@example.com");

        UserCreatedEvent event = new UserCreatedEvent(this, "1001", name, email);
        assertThat(event.getEventTimestamp()).isPositive();
    }

    @Test
    @DisplayName("getEventType()应返回类名")
    void should_getEventType() {
        UserName name = new UserName("test", "user");
        Email email = new Email("test@example.com");

        UserCreatedEvent event = new UserCreatedEvent(this, "1001", name, email);
        assertThat(event.getEventType()).isEqualTo("UserCreatedEvent");
    }

    @Test
    @DisplayName("toString()应包含关键字段")
    void should_toString() {
        UserName name = new UserName("test", "user");
        Email email = new Email("test@example.com");

        UserCreatedEvent event = new UserCreatedEvent(this, "1001", name, email);
        String str = event.toString();

        assertThat(str).contains("UserCreatedEvent");
        assertThat(str).contains("1001");
    }

    @Test
    @DisplayName("UserCreatedEvent应存储用户信息")
    void should_storeUserInfo() {
        UserName name = new UserName("张", "三");
        Email email = new Email("zhangsan@example.com");

        UserCreatedEvent event = new UserCreatedEvent(this, "U001", name, email);

        assertThat(event.getUserId()).isEqualTo("U001");
        assertThat(event.getName().getFullName()).isEqualTo("张三");
        assertThat(event.getEmail().value()).isEqualTo("zhangsan@example.com");
    }

    @Test
    @DisplayName("BaseDomainEvent的source应正确")
    void should_getSource() {
        UserCreatedEvent event = new UserCreatedEvent(this, "1001",
                new UserName("t", "u"), new Email("t@t.com"));
        assertThat(event.getSource()).isEqualTo(this);
    }
}

package com.wsf.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("EventBusDemoController 测试")
class EventBusDemoControllerTest {

    @Test
    @DisplayName("应创建控制器实例")
    void should_createInstance() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        EventBusDemoController controller = new EventBusDemoController(publisher);
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("/api/event/status 应返回事件状态信息")
    void should_getEventStatus() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        EventBusDemoController controller = new EventBusDemoController(publisher);

        String status = controller.getEventStatus();
        assertThat(status).contains("事件系统状态");
    }
}

package com.wsf.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TestController 测试")
class TestControllerTest {

    @Test
    @DisplayName("应创建控制器实例")
    void should_createInstance() {
        TestController controller = new TestController();
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("/test/test 应返回test字符串")
    void should_returnTest() {
        TestController controller = new TestController();
        var response = controller.test();
        assertThat(response.getBody()).isEqualTo("test");
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}

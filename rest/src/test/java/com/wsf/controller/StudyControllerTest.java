package com.wsf.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StudyController 测试")
class StudyControllerTest {

    @Test
    @DisplayName("应创建控制器实例")
    void should_createInstance() {
        StudyController controller = new StudyController();
        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("/hello 应返回非空响应")
    void should_returnHelloWorld() {
        StudyController controller = new StudyController();
        var response = controller.HelloWorld();
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isEqualTo("你好，世界！");
    }
}

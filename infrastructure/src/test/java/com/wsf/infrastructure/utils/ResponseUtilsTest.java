package com.wsf.infrastructure.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ResponseUtils 工具类测试")
class ResponseUtilsTest {

    @Test
    @DisplayName("ResponseUtils 类应可加载")
    void should_loadClass() {
        assertThat(ResponseUtils.class).isNotNull();
    }

    @Test
    @DisplayName("writeSuccessMsg 方法应存在")
    void should_haveWriteSuccessMethod() throws Exception {
        var method = ResponseUtils.class.getMethod("writeSuccessMsg",
                jakarta.servlet.http.HttpServletResponse.class, Object.class);
        assertThat(method).isNotNull();
    }

    @Test
    @DisplayName("writeErrMsg 方法应存在")
    void should_haveWriteErrMethod() throws Exception {
        var method = ResponseUtils.class.getMethod("writeErrMsg",
                jakarta.servlet.http.HttpServletResponse.class,
                com.wsf.infrastructure.common.result.ResultCode.class);
        assertThat(method).isNotNull();
    }
}

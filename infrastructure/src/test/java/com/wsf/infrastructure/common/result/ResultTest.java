package com.wsf.infrastructure.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Result 统一响应测试")
class ResultTest {

    @Test
    @DisplayName("应创建成功响应")
    void should_createSuccessResult() {
        Result<String> result = Result.success("data");
        assertThat(result.getCode()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(result.getData()).isEqualTo("data");
    }

    @Test
    @DisplayName("应创建失败响应")
    void should_createFailureResult() {
        Result<String> result = Result.failed(ResultCode.SYSTEM_ERROR);
        assertThat(result.getCode()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(result.getData()).isNull();
    }
}

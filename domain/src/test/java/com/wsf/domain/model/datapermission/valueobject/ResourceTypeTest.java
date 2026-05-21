package com.wsf.domain.model.datapermission.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ResourceType 枚举测试")
class ResourceTypeTest {

    @Test
    @DisplayName("应包含部门资源类型")
    void should_contain_dept() {
        assertThat(ResourceType.DEPT).isNotNull();
    }
}

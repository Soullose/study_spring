package com.wsf.api.dto.datapermission;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataPermissionDto 测试")
class DataPermissionDtoTest {

    @Test
    @DisplayName("应通过Builder构造DTO")
    void should_buildDto() {
        DataPermissionDto dto = DataPermissionDto.builder()
                .id("DP001")
                .name("全部数据")
                .dataScope("ALL")
                .resourceIds(Set.of("D01"))
                .enabled(true)
                .build();

        assertThat(dto.getId()).isEqualTo("DP001");
        assertThat(dto.getDataScope()).isEqualTo("ALL");
        assertThat(dto.getEnabled()).isTrue();
    }
}

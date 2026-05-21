package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.role.aggregate.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RoleConverter 转换器测试")
class RoleConverterTest {

    private final RoleConverter converter = new RoleConverter() {
        @Override
        public com.wsf.infrastructure.persistence.entity.role.Role toPO(Role role) {
            return null;
        }
    };

    @Test
    @DisplayName("应转换 PO → Domain")
    void should_convertPOToDomain() {
        com.wsf.infrastructure.persistence.entity.role.Role po = new com.wsf.infrastructure.persistence.entity.role.Role();
        po.setId("R002");
        po.setName("用户");
        po.setCode("USER");

        Role domain = converter.toDomain(po);
        assertThat(domain.getId()).isEqualTo("R002");
        assertThat(domain.getCode().value()).isEqualTo("USER");
    }

    @Test
    @DisplayName("应返回null when PO为null")
    void should_returnNull_when_POisNull() {
        assertThat(converter.toDomain(null)).isNull();
    }
}

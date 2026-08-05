package com.wsf.infrastructure.persistence.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.wsf.domain.model.user.aggregate.User;
import com.wsf.infrastructure.persistence.entity.user.UserPO;

@DisplayName("UserConverter 转换器测试")
class UserConverterTest {

    private final UserConverter converter = new UserConverter() {
        @Override
        public UserPO toPO(User user) {
            return null;
        }
    };

    @Test
    @DisplayName("应转换 PO → Domain")
    void should_convertPOToDomain() {
        UserPO po = new UserPO();
        po.setId("U003");
        po.setFirstname("王");
        po.setLastname("五");
        po.setEmail("wangwu@example.com");

        User domain = converter.toDomain(po);
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo("U003");
    }

    @Test
    @DisplayName("应返回null when PO为null")
    void should_returnNull_when_POisNull() {
        assertThat(converter.toDomain(null)).isNull();
    }
}

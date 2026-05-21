package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.account.aggregate.UserAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserAccountConverter 转换器测试")
class UserAccountConverterTest {

    private final UserAccountConverter converter = new UserAccountConverter() {
        @Override
        public com.wsf.infrastructure.persistence.entity.user.UserAccount toPO(UserAccount account) {
            return null;
        }
    };

    @Test
    @DisplayName("应转换 PO → Domain")
    void should_convertPOToDomain() {
        com.wsf.infrastructure.persistence.entity.user.UserAccount po = new com.wsf.infrastructure.persistence.entity.user.UserAccount();
        po.setId("A003");
        po.setUsername("user");
        po.setPassword("hash");
        po.setEnabled(true);
        po.setAccountNonExpired(true);
        po.setAccountNonLocked(true);
        po.setCredentialsNonExpired(true);

        UserAccount domain = converter.toDomain(po);
        assertThat(domain.getId()).isEqualTo("A003");
        assertThat(domain.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("应返回null when PO为null")
    void should_returnNull_when_POisNull() {
        assertThat(converter.toDomain(null)).isNull();
    }
}

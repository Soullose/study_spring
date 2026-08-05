package com.wsf.infrastructure.persistence.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.wsf.domain.model.account.aggregate.UserAccount;
import com.wsf.infrastructure.persistence.entity.user.UserAccountPO;

@DisplayName("UserAccountConverter 转换器测试")
class UserAccountConverterTest {

    private final UserAccountConverter converter = new UserAccountConverter() {
        @Override
        public UserAccountPO toPO(UserAccount account) {
            return null;
        }
    };

    @Test
    @DisplayName("应转换 PO → Domain")
    void should_convertPOToDomain() {
        UserAccountPO po = new UserAccountPO();
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

package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.infrastructure.persistence.entity.menu.MenuPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MenuConverter 转换器测试")
class MenuConverterTest {

    private final MenuConverter converter = new MenuConverter() {
        @Override
        public MenuPO toPO(Menu menu) {
            return null;
        }
    };

    @Test
    @DisplayName("应转换 PO → Domain")
    void should_convertPOToDomain() {
        MenuPO po = new MenuPO();
        po.setId("M002");
        po.setName("系统管理");
        po.setVisible(true);
        po.setEnabled(true);

        Menu domain = converter.toDomain(po);
        assertThat(domain.getId()).isEqualTo("M002");
    }

    @Test
    @DisplayName("应返回null when PO为null")
    void should_returnNull_when_POisNull() {
        assertThat(converter.toDomain(null)).isNull();
    }
}

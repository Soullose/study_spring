package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import com.wsf.infrastructure.persistence.entity.datapermission.DataPermissionPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DataPermissionConverter 转换器测试")
class DataPermissionConverterTest {

    private final DataPermissionConverter converter = new DataPermissionConverter() {
        @Override
        public DataPermissionPO toPO(DataPermission permission) {
            return null;
        }
    };

    @Test
    @DisplayName("应转换 PO → Domain")
    void should_convertPOToDomain() {
        DataPermissionPO po = new DataPermissionPO();
        po.setId("DP002");
        po.setPermissionName("本部门");
        po.setResourceType(ResourceType.DEPT);
        po.setDataScope(DataScope.DEPT);
        po.setEnabled(true);

        DataPermission domain = converter.toDomain(po);
        assertThat(domain.getId()).isEqualTo("DP002");
        assertThat(domain.getDataScope()).isEqualTo(DataScope.DEPT);
    }

    @Test
    @DisplayName("应返回null when PO为null")
    void should_returnNull_when_POisNull() {
        assertThat(converter.toDomain(null)).isNull();
    }
}

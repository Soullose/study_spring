package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.datapermission.entity.DataPermission;
import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import com.wsf.infrastructure.persistence.entity.datapermission.DataPermissionPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 数据权限领域模型转换器
 * 使用MapStruct实现领域模型DataPermission与持久化实体DataPermissionPO之间的转换
 */
@Mapper(componentModel = "spring")
public interface DataPermissionConverter {

    DataPermissionConverter INSTANCE = Mappers.getMapper(DataPermissionConverter.class);

    /**
     * 领域模型转持久化实体
     */
    @Mapping(target = "permissionName", source = "name")
    @Mapping(target = "resourceType", source = "resourceType")
    @Mapping(target = "dataScope", source = "dataScope")
    @Mapping(target = "resourceIds", source = "resourceIds")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "enabled", source = "enabled")
    DataPermissionPO toPO(DataPermission permission);

    /**
     * 持久化实体转领域模型
     * 由于领域模型使用rebuild静态工厂方法，这里使用default方法实现
     */
    default DataPermission toDomain(DataPermissionPO po) {
        if (po == null) {
            return null;
        }
        
        ResourceType resourceType = po.getResourceType() != null ? po.getResourceType() : ResourceType.DEPT;
        DataScope dataScope = po.getDataScope() != null ? po.getDataScope() : DataScope.SELF;
        boolean enabled = po.getEnabled() != null ? po.getEnabled() : true;
        
        return DataPermission.rebuild(
            po.getId(),
            po.getPermissionName(),
            resourceType,
            dataScope,
            po.getResourceIds(),
            po.getDescription(),
            enabled,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}

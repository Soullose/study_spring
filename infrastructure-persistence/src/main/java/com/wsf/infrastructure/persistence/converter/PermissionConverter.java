package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.permission.entity.Permission;
import com.wsf.infrastructure.persistence.entity.permission.PermissionPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 权限领域模型转换器
 * 使用MapStruct实现领域模型Permission与持久化实体PermissionPO之间的转换
 */
@Mapper(componentModel = "spring")
public interface PermissionConverter {

    PermissionConverter INSTANCE = Mappers.getMapper(PermissionConverter.class);

    /**
     * 领域模型转持久化实体
     */
    @Mapping(target = "permissionCode", source = "code")
    @Mapping(target = "permissionName", source = "name")
    @Mapping(target = "resource", source = "resource")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "menuId", source = "menuId")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "enabled", source = "enabled")
    PermissionPO toPO(Permission permission);

    /**
     * 持久化实体转领域模型
     * 由于领域模型使用rebuild静态工厂方法，这里使用default方法实现
     */
    default Permission toDomain(PermissionPO po) {
        if (po == null) {
            return null;
        }

        boolean enabled = po.getEnabled() != null ? po.getEnabled() : true;

        return Permission.rebuild(
            po.getId(),
            po.getPermissionCode(),
            po.getPermissionName(),
            po.getResource(),
            po.getAction(),
            po.getMenuId(),
            po.getDescription(),
            enabled,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
}

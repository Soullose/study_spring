package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.role.aggregate.Role;
import com.wsf.domain.model.role.valueobject.RoleCode;
import com.wsf.domain.model.role.valueobject.RoleName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 角色领域模型转换器
 * 使用MapStruct实现领域模型Role与持久化实体Role之间的转换
 */
@Mapper(componentModel = "spring")
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    /**
     * 领域模型转持久化实体
     */
    @Mapping(target = "name", source = "name", qualifiedByName = "roleNameToValue")
    @Mapping(target = "code", source = "code", qualifiedByName = "roleCodeToValue")
    @Mapping(target = "userAccounts", ignore = true)
    com.wsf.infrastructure.persistence.entity.role.Role toPO(Role role);

    /**
     * 持久化实体转领域模型
     * 由于领域模型使用rebuild静态工厂方法，这里使用default方法实现
     */
    default Role toDomain(com.wsf.infrastructure.persistence.entity.role.Role po) {
        if (po == null) {
            return null;
        }
        
        RoleCode code = po.getCode() != null ? new RoleCode(po.getCode()) : null;
        RoleName name = po.getName() != null ? new RoleName(po.getName()) : null;
        
        return Role.rebuild(
            po.getId(),
            code,
            name,
            null, // description
            true, // enabled
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    /**
     * RoleName值对象转字符串
     */
    @Named("roleNameToValue")
    default String roleNameToValue(RoleName name) {
        return name != null ? name.value() : null;
    }

    /**
     * RoleCode值对象转字符串
     */
    @Named("roleCodeToValue")
    default String roleCodeToValue(RoleCode code) {
        return code != null ? code.value() : null;
    }
}

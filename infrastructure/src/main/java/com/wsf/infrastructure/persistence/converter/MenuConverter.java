package com.wsf.infrastructure.persistence.converter;

import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.menu.valueobject.MenuStatus;
import com.wsf.domain.model.menu.valueobject.MenuType;
import com.wsf.infrastructure.persistence.entity.menu.MenuPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

/**
 * 菜单领域模型转换器
 * 使用MapStruct实现领域模型Menu与持久化实体MenuPO之间的转换
 */
@Mapper(componentModel = "spring")
public interface MenuConverter {

    MenuConverter INSTANCE = Mappers.getMapper(MenuConverter.class);

    /**
     * 领域模型转持久化实体
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "parentId", source = "parentId")
    @Mapping(target = "menuType", source = "menuType")
    @Mapping(target = "path", source = "path")
    @Mapping(target = "component", source = "component")
    @Mapping(target = "perms", source = "permission")
    @Mapping(target = "icon", source = "icon")
    @Mapping(target = "sortOrder", source = "sortOrder")
    @Mapping(target = "visible", source = "status", qualifiedByName = "statusToVisible")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToEnabled")
    @Mapping(target = "externalLink", source = "externalLink")
    @Mapping(target = "cacheEnabled", source = "cacheEnabled")
    @Mapping(target = "roles", ignore = true)
    MenuPO toPO(Menu menu);

    /**
     * 持久化实体转领域模型
     * 由于领域模型使用rebuild静态工厂方法，这里使用default方法实现
     */
    default Menu toDomain(MenuPO po) {
        if (po == null) {
            return null;
        }
        
        MenuType menuType = po.getMenuType() != null ? po.getMenuType() : MenuType.MENU;
        MenuStatus status = new MenuStatus(
            po.getVisible() != null ? po.getVisible() : true,
            po.getStatus() != null ? po.getStatus() : true
        );
        
        return Menu.rebuild(
            po.getId(),
            po.getName(),
            po.getParentId(),
            menuType,
            po.getPath(),
            po.getComponent(),
            po.getPerms(),
            po.getIcon(),
            po.getSortOrder() != null ? po.getSortOrder() : 0,
            status,
            po.getExternalLink(),
            po.getCacheEnabled() != null ? po.getCacheEnabled() : false,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    /**
     * MenuStatus转visible
     */
    @Named("statusToVisible")
    default Boolean statusToVisible(MenuStatus status) {
        return status != null && status.isVisible();
    }

    /**
     * MenuStatus转enabled
     */
    @Named("statusToEnabled")
    default Boolean statusToEnabled(MenuStatus status) {
        return status != null && status.enabled();
    }
}

package com.wsf.app.service.impl;

import com.wsf.api.dto.menu.*;
import com.wsf.api.service.MenuService;
import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.menu.valueobject.MenuType;
import com.wsf.domain.repository.MenuRepository;
import com.wsf.infrastructure.jpa.id.CustomIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    @Override
    @Transactional
    public MenuDto createMenu(CreateMenuRequest request) {
        String menuId = CustomIdGenerator.generateId();
        
        Menu menu = switch (request.getMenuType()) {
            case "DIR" -> Menu.createDirectory(
                    menuId, 
                    request.getName(), 
                    request.getParentId(),
                    request.getIcon(), 
                    request.getSortOrder()
            );
            case "MENU" -> Menu.createMenu(
                    menuId, 
                    request.getName(), 
                    request.getParentId(),
                    request.getPath(), 
                    request.getComponent(),
                    request.getPermission(), 
                    request.getIcon(), 
                    request.getSortOrder()
            );
            case "BUTTON" -> Menu.createButton(
                    menuId, 
                    request.getName(), 
                    request.getParentId(),
                    request.getPermission(), 
                    request.getSortOrder()
            );
            default -> throw new IllegalArgumentException("无效的菜单类型: " + request.getMenuType());
        };
        
        Menu savedMenu = menuRepository.save(menu);
        return toDto(savedMenu);
    }

    @Override
    @Transactional
    public MenuDto updateMenu(String menuId, UpdateMenuRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + menuId));
        
        menu.update(
                request.getName(),
                request.getPath(),
                request.getComponent(),
                request.getPermission(),
                request.getIcon(),
                request.getSortOrder()
        );
        
        Menu savedMenu = menuRepository.save(menu);
        return toDto(savedMenu);
    }

    @Override
    public Optional<MenuDto> findById(String menuId) {
        return menuRepository.findById(menuId)
                .map(this::toDto);
    }

    @Override
    public List<MenuDto> findAll() {
        return menuRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<MenuDto> findAllEnabled() {
        return menuRepository.findAllEnabled().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<MenuDto> getMenuTree() {
        List<Menu> allMenus = menuRepository.findAll();
        List<Menu> menuTree = menuRepository.buildMenuTree(allMenus);
        return menuTree.stream()
                .map(this::toDtoWithChildren)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMenu(String menuId) {
        menuRepository.deleteById(menuId);
    }

    @Override
    @Transactional
    public MenuDto showMenu(String menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + menuId));
        
        menu.show();
        Menu savedMenu = menuRepository.save(menu);
        return toDto(savedMenu);
    }

    @Override
    @Transactional
    public MenuDto hideMenu(String menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + menuId));
        
        menu.hide();
        Menu savedMenu = menuRepository.save(menu);
        return toDto(savedMenu);
    }

    @Override
    @Transactional
    public MenuDto enableMenu(String menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + menuId));
        
        menu.enable();
        Menu savedMenu = menuRepository.save(menu);
        return toDto(savedMenu);
    }

    @Override
    @Transactional
    public MenuDto disableMenu(String menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + menuId));
        
        menu.disable();
        Menu savedMenu = menuRepository.save(menu);
        return toDto(savedMenu);
    }
    
    /**
     * 转换为DTO（不含子菜单）
     */
    private MenuDto toDto(Menu menu) {
        return MenuDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .parentId(menu.getParentId())
                .menuType(menu.getMenuType() != null ? menu.getMenuType().name() : null)
                .path(menu.getPath())
                .component(menu.getComponent())
                .permission(menu.getPermission())
                .icon(menu.getIcon())
                .sortOrder(menu.getSortOrder())
                .visible(menu.getStatus() != null && menu.getStatus().isVisible())
                .enabled(menu.getStatus() != null && menu.getStatus().enabled())
                .externalLink(menu.getExternalLink())
                .cacheEnabled(menu.isCacheEnabled())
                .createTime(menu.getCreateTime())
                .updateTime(menu.getUpdateTime())
                .build();
    }
    
    /**
     * 转换为DTO（含子菜单）
     */
    private MenuDto toDtoWithChildren(Menu menu) {
        MenuDto dto = toDto(menu);
        
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<MenuDto> children = menu.getChildren().stream()
                    .map(this::toDtoWithChildren)
                    .toList();
            dto.setChildren(children);
        }
        
        return dto;
    }
}

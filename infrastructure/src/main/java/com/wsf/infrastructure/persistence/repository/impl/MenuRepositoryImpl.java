package com.wsf.infrastructure.persistence.repository.impl;

import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.model.menu.valueobject.MenuType;
import com.wsf.domain.repository.MenuRepository;
import com.wsf.infrastructure.persistence.converter.MenuConverter;
import com.wsf.infrastructure.persistence.repository.MenuJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单仓储实现
 */
@Repository
@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepository {

    private final MenuJpaRepository jpaRepository;
    private final MenuConverter converter;

    @Override
    public Menu save(Menu menu) {
        var po = converter.toPO(menu);
        var savedPo = jpaRepository.save(po);
        return converter.toDomain(savedPo);
    }

    @Override
    public List<Menu> saveAll(List<Menu> menus) {
        var pos = menus.stream()
                .map(converter::toPO)
                .toList();
        var savedPos = jpaRepository.saveAll(pos);
        return savedPos.stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Optional<Menu> findById(String id) {
        return jpaRepository.findById(id)
                .map(converter::toDomain);
    }

    @Override
    public List<Menu> findAll() {
        return jpaRepository.findAllByOrderBySortOrderAsc().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Menu> findByIds(List<String> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public Set<Menu> findByIds(Set<String> ids) {
        return jpaRepository.findByIdIn(ids).stream()
                .map(converter::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Menu> findRootMenus() {
        return jpaRepository.findRootMenus().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Menu> findByParentId(String parentId) {
        return jpaRepository.findByParentIdOrderBySortOrderAsc(parentId).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Menu> findByMenuType(MenuType menuType) {
        return jpaRepository.findByMenuTypeOrderBySortOrderAsc(menuType).stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Menu> findAllEnabled() {
        return jpaRepository.findByEnabledTrueOrderBySortOrderAsc().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Menu> findAllEnabledOrderBySortOrder() {
        return jpaRepository.findByEnabledTrueOrderBySortOrderAsc().stream()
                .map(converter::toDomain)
                .toList();
    }

    @Override
    public List<Menu> buildMenuTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        
        // 构建ID到菜单的映射
        Map<String, Menu> menuMap = new HashMap<>();
        menus.forEach(menu -> menuMap.put(menu.getId(), menu));
        
        // 构建树形结构
        List<Menu> rootMenus = new ArrayList<>();
        for (Menu menu : menus) {
            String parentId = menu.getParentId();
            if (parentId == null || parentId.isBlank()) {
                // 根菜单
                rootMenus.add(menu);
            } else {
                // 添加到父菜单的子菜单列表
                Menu parentMenu = menuMap.get(parentId);
                if (parentMenu != null) {
                    parentMenu.addChild(menu);
                }
            }
        }
        
        // 按排序号排序
        rootMenus.sort(Comparator.comparing(Menu::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())));
        
        return rootMenus;
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    /**
     * 检查同一父菜单下是否存在指定名称的菜单
     * 这是内部辅助方法，不在仓储接口中定义
     */
    public boolean existsByParentIdAndName(String parentId, String name) {
        return jpaRepository.existsByNameAndParentId(name, parentId != null ? parentId : "");
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean hasChildren(String menuId) {
        return jpaRepository.findByParentIdOrderBySortOrderAsc(menuId).size() > 0;
    }
}

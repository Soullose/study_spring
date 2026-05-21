package com.wsf.app.service.impl;

import com.wsf.api.dto.menu.CreateMenuRequest;
import com.wsf.api.dto.menu.MenuDto;
import com.wsf.api.dto.menu.UpdateMenuRequest;
import com.wsf.domain.model.menu.aggregate.Menu;
import com.wsf.domain.repository.MenuRepository;
import com.wsf.domain.service.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuServiceImpl 单元测试")
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
        lenient().when(idGenerator.generate()).thenReturn("MENU-GEN-001");
    }

    @Test
    @DisplayName("应创建目录菜单")
    void should_createDirMenu() {
        CreateMenuRequest req = new CreateMenuRequest();
        req.setMenuType("DIR");
        req.setName("系统管理");
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuDto result = menuService.createMenu(req);
        assertThat(result.getName()).isEqualTo("系统管理");
        assertThat(result.getMenuType()).isEqualTo("DIR");
    }

    @Test
    @DisplayName("应创建页面菜单")
    void should_createPageMenu() {
        CreateMenuRequest req = new CreateMenuRequest();
        req.setMenuType("MENU");
        req.setName("用户管理");
        req.setPath("/users");
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuDto result = menuService.createMenu(req);
        assertThat(result.getMenuType()).isEqualTo("MENU");
        assertThat(result.getPath()).isEqualTo("/users");
    }

    @Test
    @DisplayName("应创建按钮菜单")
    void should_createButtonMenu() {
        CreateMenuRequest req = new CreateMenuRequest();
        req.setMenuType("BUTTON");
        req.setName("新增");
        req.setPermission("sys:user:create");
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuDto result = menuService.createMenu(req);
        assertThat(result.getMenuType()).isEqualTo("BUTTON");
        assertThat(result.getPermission()).isEqualTo("sys:user:create");
    }

    @Test
    @DisplayName("应抛出异常 when 菜单类型无效")
    void should_throwException_when_invalidType() {
        CreateMenuRequest req = new CreateMenuRequest();
        req.setMenuType("INVALID");
        assertThatThrownBy(() -> menuService.createMenu(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的菜单类型");
    }

    @Test
    @DisplayName("应更新菜单")
    void should_updateMenu() {
        Menu menu = Menu.createMenu("M001", "旧名称", null, "/old", "Old", "old:view", "old", 1);
        when(menuRepository.findById("M001")).thenReturn(Optional.of(menu));
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateMenuRequest req = new UpdateMenuRequest();
        req.setName("新名称");
        req.setPath("/new");

        MenuDto result = menuService.updateMenu("M001", req);
        assertThat(result.getName()).isEqualTo("新名称");
    }

    @Test
    @DisplayName("应显示菜单")
    void should_showMenu() {
        Menu menu = Menu.createMenu("M001", "test", null, "/test", "Test", "test:view", "test", 1);
        menu.hide();
        when(menuRepository.findById("M001")).thenReturn(Optional.of(menu));
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuDto result = menuService.showMenu("M001");
        assertThat(result.getVisible()).isTrue();
    }

    @Test
    @DisplayName("应启用菜单")
    void should_enableMenu() {
        Menu menu = Menu.createMenu("M001", "test", null, "/test", "Test", "test:view", "test", 1);
        menu.disable();
        when(menuRepository.findById("M001")).thenReturn(Optional.of(menu));
        when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuDto result = menuService.enableMenu("M001");
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("应查找所有菜单")
    void should_findAll() {
        Menu m1 = Menu.createMenu("M001", "a", null, "/a", "A", "a:view", "a", 1);
        Menu m2 = Menu.createMenu("M002", "b", null, "/b", "B", "b:view", "b", 1);
        when(menuRepository.findAll()).thenReturn(List.of(m1, m2));

        assertThat(menuService.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("应返回菜单树")
    void should_getMenuTree() {
        Menu root = Menu.createDirectory("M001", "根", null, "root", 0);
        when(menuRepository.findAll()).thenReturn(List.of(root));
        when(menuRepository.buildMenuTree(any())).thenReturn(List.of(root));

        List<MenuDto> tree = menuService.getMenuTree();
        assertThat(tree).hasSize(1);
    }
}

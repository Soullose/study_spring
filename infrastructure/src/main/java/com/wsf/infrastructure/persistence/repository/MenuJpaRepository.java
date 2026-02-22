package com.wsf.infrastructure.persistence.repository;

import com.wsf.domain.model.menu.valueobject.MenuType;
import com.wsf.infrastructure.persistence.entity.menu.MenuPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 菜单JPA仓储
 */
@Repository
public interface MenuJpaRepository 
        extends JpaRepository<MenuPO, String>, 
                JpaSpecificationExecutor<MenuPO>,
                QuerydslPredicateExecutor<MenuPO> {
    
    /**
     * 根据父菜单ID查找子菜单列表
     */
    List<MenuPO> findByParentIdOrderBySortOrderAsc(String parentId);
    
    /**
     * 查找根菜单列表（无父菜单）
     */
    @Query("SELECT m FROM MenuPO m WHERE m.parentId IS NULL OR m.parentId = '' ORDER BY m.sortOrder ASC")
    List<MenuPO> findRootMenus();
    
    /**
     * 根据菜单类型查找菜单列表
     */
    List<MenuPO> findByMenuTypeOrderBySortOrderAsc(MenuType menuType);
    
    /**
     * 查找所有可见的菜单
     */
    List<MenuPO> findByVisibleTrueOrderBySortOrderAsc();
    
    /**
     * 查找所有启用的菜单
     */
    List<MenuPO> findByEnabledTrueOrderBySortOrderAsc();
    
    /**
     * 根据权限标识查找菜单
     */
    Optional<MenuPO> findByPerms(String perms);
    
    /**
     * 根据路径查找菜单
     */
    Optional<MenuPO> findByPath(String path);
    
    /**
     * 检查菜单名称是否存在（同一父菜单下）
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MenuPO m WHERE m.name = :name AND m.parentId = :parentId")
    boolean existsByNameAndParentId(String name, String parentId);
    
    /**
     * 检查菜单名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据角色ID查找菜单列表
     */
    @Query("SELECT m FROM MenuPO m JOIN m.roles r WHERE r.id = :roleId ORDER BY m.sortOrder ASC")
    Set<MenuPO> findByRoleId(String roleId);
    
    /**
     * 根据账户ID查找补充菜单列表
     */
    @Query("SELECT m FROM MenuPO m JOIN m.userAccounts ua WHERE ua.id = :userAccountId ORDER BY m.sortOrder ASC")
    Set<MenuPO> findSupplementaryMenusByUserAccountId(String userAccountId);
    
    /**
     * 查找所有菜单（按排序号排序）
     */
    List<MenuPO> findAllByOrderBySortOrderAsc();
    
    /**
     * 根据多个ID查找菜单列表
     */
    Set<MenuPO> findByIdIn(Set<String> ids);
}

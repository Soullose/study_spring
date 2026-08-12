package com.wsf.infrastructure.persistence.entity.menu;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Comment;

import com.wsf.domain.model.menu.valueobject.MenuType;
import com.wsf.infrastructure.persistence.entity.BaseEntity;
import com.wsf.infrastructure.persistence.entity.role.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 菜单持久化实体
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_OPEN_MENU_")
@Comment("菜单表")
public class MenuPO extends BaseEntity {

    /**
     * 菜单名称
     */
    @Column(name = "menu_name_", nullable = false)
    private String name;

    /**
     * 父菜单ID
     */
    @Column(name = "parent_id_", length = 64)
    private String parentId;

    /**
     * 菜单类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "menu_type_", length = 20)
    private MenuType menuType;

    /**
     * 路由路径
     */
    @Column(name = "path_")
    private String path;

    /**
     * 组件路径
     */
    @Column(name = "component_")
    private String component;

    /**
     * 权限标识
     */
    @Column(name = "perms_")
    private String perms;

    /**
     * 图标
     */
    @Column(name = "icon_")
    private String icon;

    /**
     * 排序号
     */
    @Builder.Default
    @Column(name = "sort_order_")
    private Integer sortOrder = 0;

    /**
     * 是否显示
     */
    @Builder.Default
    @Column(name = "visible_")
    private Boolean visible = true;

    /**
     * 是否启用
     */
    @Builder.Default
    @Column(name = "enabled_")
    private Boolean enabled = true;

    /**
     * 外链地址
     */
    @Column(name = "external_link_", length = 200)
    private String externalLink;

    /**
     * 是否缓存
     */
    @Builder.Default
    @Column(name = "cache_enabled_")
    private Boolean cacheEnabled = false;

    /**
     * 关联的角色列表
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "T_OPEN_ROLE_MENU_", joinColumns = @JoinColumn(name = "menu_id_"), inverseJoinColumns = @JoinColumn(
                    name = "role_id_"
            )
    )

    @Builder.Default
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();
}

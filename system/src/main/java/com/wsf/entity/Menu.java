package com.wsf.entity;

import com.wsf.domain.BaseEntity;
import com.wsf.domain.entity.Role;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * open
 * SoulLose
 * 2022-05-04 16:37
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_OPEN_MENU_")
public class Menu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -6489977862733269490L;

    @Column(name = "menu_name_")
    private String name;

    @Column(name = "path_")
    private String path;

    @Column(name = "component_")
    private String component;

    @Column(name = "visible_")
    private boolean visible = false;

    @Column(name = "status_")
    private boolean status = false;

    @Column(name = "perms_")
    private String perms;

    @Column(name = "icon_")
    private String icon;

    @Column(name = "createTime_")
    private LocalDateTime createTime;

    // @JsonIgnore
    @ManyToMany
    @JoinTable(name = "t_open_role_menu_", joinColumns = { @JoinColumn(name = "menu_id_") }, inverseJoinColumns = {
            @JoinColumn(name = "role_id_") })
    private Set<Role> roles;
}

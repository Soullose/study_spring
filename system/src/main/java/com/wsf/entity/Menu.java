package com.wsf.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wsf.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * open
 * SoulLose
 * 2022-05-04 16:37
 */
@Getter
@Setter
@Entity
@Table(name = "t_open_menu_")
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
    private Date createTime;
    
//    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "t_open_role_menu_", joinColumns = {@JoinColumn(name = "menu_id_")}, inverseJoinColumns = {@JoinColumn(name = "role_id_")})
    private Set<Role> roles;
}

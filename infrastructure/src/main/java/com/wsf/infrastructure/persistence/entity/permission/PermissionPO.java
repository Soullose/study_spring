package com.wsf.infrastructure.persistence.entity.permission;

import com.wsf.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * 权限持久化实体
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_OPEN_PERMISSION_")
@Comment("权限表")
public class PermissionPO extends BaseEntity {
    
    /**
     * 权限编码
     */
    @Column(name = "permission_code_", unique = true, nullable = false, length = 100)
    private String permissionCode;
    
    /**
     * 权限名称
     */
    @Column(name = "permission_name_", nullable = false, length = 100)
    private String permissionName;
    
    /**
     * 资源标识
     */
    @Column(name = "resource_", length = 200)
    private String resource;
    
    /**
     * 操作类型
     */
    @Column(name = "action_", length = 50)
    private String action;
    
    /**
     * 关联菜单ID
     */
    @Column(name = "menu_id_", length = 64)
    private String menuId;
    
    /**
     * 描述
     */
    @Column(name = "description_", length = 500)
    private String description;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled_")
    private Boolean enabled = true;
}

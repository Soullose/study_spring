package com.wsf.infrastructure.persistence.entity.datapermission;

import com.wsf.domain.model.datapermission.valueobject.DataScope;
import com.wsf.domain.model.datapermission.valueobject.ResourceType;
import com.wsf.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * 数据权限持久化实体
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "T_OPEN_DATA_PERMISSION_")
@Comment("数据权限表")
public class DataPermissionPO extends BaseEntity {
    
    /**
     * 权限名称
     */
    @Column(name = "permission_name_", nullable = false, length = 100)
    private String permissionName;
    
    /**
     * 资源类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type_", nullable = false, length = 50)
    private ResourceType resourceType;
    
    /**
     * 数据范围
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_scope_", nullable = false, length = 20)
    private DataScope dataScope;
    
    /**
     * 自定义资源ID列表（逗号分隔）
     */
    @Column(name = "resource_ids_", columnDefinition = "TEXT")
    private String resourceIds;
    
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

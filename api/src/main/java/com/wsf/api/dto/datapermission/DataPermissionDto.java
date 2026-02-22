package com.wsf.api.dto.datapermission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 数据权限响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPermissionDto {
    
    /**
     * 数据权限ID
     */
    private String id;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 资源类型: DEPT/ORG/CUSTOM
     */
    private String resourceType;
    
    /**
     * 数据范围: ALL/DEPT/DEPT_AND_BELOW/SELF/CUSTOM
     */
    private String dataScope;
    
    /**
     * 自定义资源ID列表
     */
    private Set<String> resourceIds;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

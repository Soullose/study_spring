package com.wsf.api.dto.datapermission;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 创建数据权限请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDataPermissionRequest {
    
    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    private String name;
    
    /**
     * 资源类型: DEPT/ORG/CUSTOM
     */
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;
    
    /**
     * 数据范围: ALL/DEPT/DEPT_AND_BELOW/SELF/CUSTOM
     */
    @NotBlank(message = "数据范围不能为空")
    private String dataScope;
    
    /**
     * 自定义资源ID列表（当dataScope为CUSTOM时必填）
     */
    private Set<String> resourceIds;
    
    /**
     * 描述
     */
    private String description;
}

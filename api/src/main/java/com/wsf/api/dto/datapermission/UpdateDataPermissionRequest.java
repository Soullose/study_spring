package com.wsf.api.dto.datapermission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 更新数据权限请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDataPermissionRequest {
    
    /**
     * 权限名称
     */
    private String name;
    
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
}

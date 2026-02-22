package com.wsf.api.service;

import com.wsf.api.dto.datapermission.*;

import java.util.List;
import java.util.Optional;

/**
 * 数据权限服务接口
 */
public interface DataPermissionService {
    
    /**
     * 创建数据权限
     */
    DataPermissionDto createPermission(CreateDataPermissionRequest request);
    
    /**
     * 更新数据权限
     */
    DataPermissionDto updatePermission(String permissionId, UpdateDataPermissionRequest request);
    
    /**
     * 根据ID查找数据权限
     */
    Optional<DataPermissionDto> findById(String permissionId);
    
    /**
     * 查找所有数据权限
     */
    List<DataPermissionDto> findAll();
    
    /**
     * 查找启用的数据权限
     */
    List<DataPermissionDto> findAllEnabled();
    
    /**
     * 删除数据权限
     */
    void deletePermission(String permissionId);
    
    /**
     * 启用数据权限
     */
    DataPermissionDto enablePermission(String permissionId);
    
    /**
     * 禁用数据权限
     */
    DataPermissionDto disablePermission(String permissionId);
}

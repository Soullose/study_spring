package com.wsf.domain.service;

/**
 * ID生成器接口
 * 领域服务接口，由基础设施层实现
 * 用于为领域对象生成唯一标识符
 */
public interface IdGenerator {
    
    /**
     * 生成唯一ID
     * @return 唯一ID字符串
     */
    String generate();
}

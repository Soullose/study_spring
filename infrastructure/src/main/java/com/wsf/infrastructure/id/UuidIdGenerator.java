package com.wsf.infrastructure.id;

import com.wsf.domain.service.IdGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;

import com.github.f4b6a3.uuid.alt.GUID;

/**
 * UUID ID生成器实现
 * 使用GUID v7生成时间有序的UUID
 * 实现domain层的IdGenerator接口
 */
@Component
public class UuidIdGenerator implements IdGenerator {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    @Override
    public String generate() {
        GUID guid = GUID.v7(Instant.now(), SECURE_RANDOM);
        return guid.toString();
    }
}

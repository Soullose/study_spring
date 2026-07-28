package com.wsf.infrastructure.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.wsf.infrastructure.security.repository.UserAccountPORepository;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class WarmupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(WarmupRunner.class);
    private final RedissonClient redissonClient;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountPORepository userAccountPORepository;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long start = System.currentTimeMillis();
        // 1. 预热 Hibernate（JPA 查询 → 触发实体元模型初始化 + 查询计划缓存 + DB 连接池）
        try {
            userAccountPORepository.count();
        } catch (Exception e) {
            log.warn("Hibernate warmup failed", e);
        }
        // 2. 预热 Redis 连接
        try {
            redissonClient.getBucket("warmup:ping").set("1", Duration.ofSeconds(5));
            redissonClient.getBucket("warmup:ping").delete();
        } catch (Exception e) {
            log.warn("Redis warmup failed", e);
        }
        // 3. 预热 Argon2 / BouncyCastle 类加载
        try {
            passwordEncoder.encode("warmup");
        } catch (Exception e) {
            log.warn("PasswordEncoder warmup failed", e);
        }
        // 4. 预热 jjwt 签名（含 signWith → 解码 Base64 密钥 + 构建 HMAC SecretKey）
        try {
            Jwts.builder()
                    .subject("warmup")
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)), Jwts.SIG.HS256)
                    .compact();
        } catch (Exception e) {
            log.warn("JWT warmup failed", e);
        }
        log.info("Warmup completed in {}ms", System.currentTimeMillis() - start);
    }
}

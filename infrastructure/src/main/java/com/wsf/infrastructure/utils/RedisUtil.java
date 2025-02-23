package com.wsf.infrastructure.utils;

import org.redisson.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis工具类
 */
@Component
public class RedisUtil {
    private static Logger log = LoggerFactory.getLogger(RedisUtil.class);


    private final RedissonClient redissonClient;

    public RedisUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 移除缓存
     *
     * @param key 键
     */
    public void delete(String key) {
        redissonClient.getBucket(key).delete();
    }

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 用于操作key
     *
     * @return RKeys 对象
     */
    public RKeys getKeys() {
        return redissonClient.getKeys();
    }

    /**
     * 获取getBuckets 对象
     *
     * @return RBuckets 对象
     */
    public RBuckets getBuckets() {
        return redissonClient.getBuckets();
    }

    /**
     * @param key   键
     * @param clazz 类
     * @param <T>   {@link Class<T>}
     * @return {@link T}
     */
    public <T> T get(String key, Class<T> clazz) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     *
     * @param key
     * @param value
     * @param ttl
     * @return
     * @param <T>
     */
    public <T> T set(String key, T value, long ttl) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, Duration.ofSeconds(ttl));
        return value;
    }

    /**
     * 缓存字符串
     *
     * @param key   键
     * @param value 值
     */
    public void setStr(String key, String value, long ttl) {
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.setIfAbsent(value, Duration.ofSeconds(ttl));
    }

    /**
     * 增加
     *
     * @param key 键
     * @return {@link Long}
     */
    public Long increment(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    /**
     * 减少
     *
     * @param key 键
     * @return {@link Long}
     */
    public Long decrement(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }
}

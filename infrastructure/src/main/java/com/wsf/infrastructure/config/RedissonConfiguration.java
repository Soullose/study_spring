package com.wsf.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RedissonConfiguration {
    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.data.redis")
    RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());
        SingleServerConfig singleConfig = config.useSingleServer();
        singleConfig.setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort());
        singleConfig.setPassword(redisProperties.getPassword());
        return Redisson.create(config);
    }
}

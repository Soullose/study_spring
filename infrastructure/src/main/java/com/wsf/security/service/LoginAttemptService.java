package com.wsf.security.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.BaseEncoding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginAttemptService {
    
    private final int MAX_ATTEMPT = 10;
    private LoadingCache<String, Integer> attemptsCache;
    
    public LoginAttemptService() {
        super();
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .recordStats()
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(final String key) {
                        return 0;
                    }
                });
    }
    
    //
    
    public void loginSucceeded(final String key) {
        attemptsCache.invalidate(key2Base64(key));
    }
    
    public void loginFailed(final String key) {
        log.info("1");
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key2Base64(key));
        } catch (final ExecutionException e) {
            log.error("context{}", e.getCause());
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key2Base64(key), attempts);
    }
    
    public boolean isBlocked(final String key) {
        try {
            return attemptsCache.get(key2Base64(key)) >= MAX_ATTEMPT;
        } catch (final ExecutionException e) {
            return false;
        }
    }
    
    public String key2Base64(String key) {
        return BaseEncoding.base64().encode(key.getBytes());
    }
}
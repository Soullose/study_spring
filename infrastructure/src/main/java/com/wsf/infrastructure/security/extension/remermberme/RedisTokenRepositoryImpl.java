package com.wsf.infrastructure.security.extension.remermberme;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.wsf.infrastructure.utils.RedisUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RedisTokenRepositoryImpl implements PersistentTokenRepository {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenRepositoryImpl.class);


    private final String SERIES_PREFIX = "spring:security:rememberMe:series:";
    private final String USERNAME_PREFIX = "spring:security:rememberMe:username:";

    private final RedisUtil redisUtil;

    public RedisTokenRepositoryImpl() {
        redisUtil = new RedisUtil();
    }

    private String generateKey(String prefix, String var) {
        return prefix + var;
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        String key = generateKey(SERIES_PREFIX, token.getSeries());
        String generateKey = generateKey(USERNAME_PREFIX, token.getUsername());
        boolean hasKey = redisUtil.hasKey(key);
        if (hasKey) {
            throw new DataIntegrityViolationException("Series Id '" + token.getSeries() + "' already exists!");
        }
        redisUtil.set(key, token, 60 * 60 * 24 * 7);
        redisUtil.set(generateKey, token.getSeries(), 60 * 60 * 24 * 7);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        String key = generateKey(SERIES_PREFIX, series);
        PersistentRememberMeToken token = this.getTokenForSeries(series);
        PersistentRememberMeToken newToken = new PersistentRememberMeToken(token.getUsername(), series, tokenValue, new Date());
        redisUtil.set(key, token, 60 * 60 * 24 * 7);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        String key = generateKey(SERIES_PREFIX, seriesId);
        return (PersistentRememberMeToken)redisUtil.get(key,PersistentRememberMeToken.class);
    }

    @Override
    public void removeUserTokens(String username) {
        String key = generateKey(USERNAME_PREFIX, username);
        String series = redisUtil.get(key, String.class);
        String seriesKey = generateKey(USERNAME_PREFIX, series);
        redisUtil.delete(key);
        redisUtil.delete(seriesKey);
    }
}

package com.wsf.infrastructure.security.filter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAwareRateLimitFilter extends OncePerRequestFilter {
    /// 使用 Guava Cache 自动清理不再使用的限流器
    private final LoadingCache<String, RateLimiter> limiters = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public RateLimiter load(String key) {
                    // 配置：每秒10个请求，1秒的等待超时时间
                    RateLimiterConfig config =
                            RateLimiterConfig.custom()
                                    .limitRefreshPeriod(java.time.Duration.ofSeconds(1))
                                    .limitForPeriod(10)
                                    .timeoutDuration(java.time.Duration.ofSeconds(1))
                                    .build();

                    return RateLimiterRegistry.of(config).rateLimiter(key);
                }
            });

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 从SecurityContext中获取当前用户名
        String username = getCurrentUsername();

        // 或者基于IP地址限流
        // String clientIp = request.getRemoteAddr();

        try {
            RateLimiter userLimiter = limiters.get(username);

            // 尝试获取许可，获取不到则限流
            if (!userLimiter.acquirePermission()) {
                response.setStatus(429);
                response.getWriter().write("Rate limit exceeded for user: " + username);
                return;
            }

            filterChain.doFilter(request, response);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}

package com.wsf.infrastructure.security.filter;

import com.wsf.infrastructure.utils.RedisCache;
import com.wsf.infrastructure.security.domain.LoginUserDetail;
import com.wsf.infrastructure.utils.JwtUtil;
import com.wsf.mapstruct.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * open
 * SoulLose
 * 2022-05-01 12:08
 */
@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    
    private static final String REDIS_KEY = "open-login:";
    
    @Autowired
    private RedisCache redisCache;

    @Autowired(required = false)
    private UserMapper userMapper;
    
    
    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("1");
        //获取token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        //解析token
        String userId;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            log.error("context{}", e.getCause());
            throw new RuntimeException("token非法");
        }

        //从redis中获取用户信息
        String redisKey = REDIS_KEY + userId;
        LoginUserDetail loginUserDetail = redisCache.getCacheObject(redisKey);
        log.info("用户信息:{}", loginUserDetail);
        if (Objects.isNull(loginUserDetail)) {
            throw new RuntimeException("用户未登录");
        }

        //存入securityContextHolder
        //TODO 获取权限信息封装到 authenticationToken中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserDetail, null, loginUserDetail.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        //放行
        filterChain.doFilter(request, response);
    }
}

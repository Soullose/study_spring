package com.wsf.infrastructure.security.service.impl;

import com.wsf.mapstruct.UserMapper;
import com.wsf.params.LoginUserParams;
import com.wsf.infrastructure.utils.JwtUtil;
import com.wsf.infrastructure.utils.RedisCache;
import com.wsf.infrastructure.security.domain.LoginUserDetail;
import com.wsf.infrastructure.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * open
 * SoulLose
 * 2022-04-28 11:51
 */
@Service
@Slf4j
public class LoginUserServiceImpl implements LoginService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private RedisCache redisCache;

    @Autowired(required = false)
    private UserMapper userMapper;
    /**
     * 登录并返回jwt
     * @param params   前端传过来的登录信息
     * @return         {@link String}
     */
    @Override
    public String login(LoginUserParams params) {
        log.info("2");
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(params.getUserName(), params.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        
        //判断认证是否为空
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("登录失败");
        }

        LoginUserDetail loginUserDetail = (LoginUserDetail) authenticate.getPrincipal();
        String id = loginUserDetail.getUser().getId();
        String jwt = JwtUtil.createJWT(id);
        log.info("jwt：{}", jwt);

//        redisCache.setCacheObject("open-login:"+id,loginUserDetail,60, TimeUnit.MINUTES);
        redisCache.setCacheObject("open-login:"+id,loginUserDetail);
        return jwt;
    }
    
    @Override
    public void logout() {
        //获取SecurityContextHolder中的用户信息
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LoginUserDetail loginUserDetail = (LoginUserDetail) authentication.getPrincipal();
        String id = loginUserDetail.getUser().getId();
        //删除redis中的值
        redisCache.deleteObject("open-login:"+id);
    }
}

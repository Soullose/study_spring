package com.wsf.infrastructure.security.service;

import com.wsf.dto.UserDto;
import com.wsf.entity.*;
import com.wsf.mapstruct.UserMapper;
import com.wsf.repository.UserRepository;
import com.wsf.infrastructure.security.domain.LoginUserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

/**
 * open
 * SoulLose
 * 2022-04-24 15:11
 */
@Service
@Transactional
@Slf4j
public class OpenUserDetailsService implements UserDetailsService {
    
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    //不强制要求注入，启动后会产生HttpServletRequest的servlet
    @Autowired(required = false)
    private HttpServletRequest request;
    
    private final UserMapper userMapper;
    
    public OpenUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        QUser user_ = QUser.user;
        QRole role_ = QRole.role;
        QMenu menu_ = QMenu.menu;
        
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("IP已锁住，禁止访问");
        }
        log.info("用户名：{}", userName);
        //查询用户信息
        User user = userRepository.findUserByUserName(userName);
        UserDto userDto = userMapper.toDto(user);
        log.debug("用户:{}", user);
        if (Objects.isNull(user)) {
            log.debug("用户不存在或用户名密码错误");
//            throw new UsernameNotFoundException(String.format("用户不存在 '%s'", userName));
            throw new RuntimeException(String.format("用户不存在 '%s'", userName));
        }
        
        //TODO 查询权限信息
        List<String> fetch = userRepository.getQueryFactory().select(menu_.perms)
                .from(user_)
                .leftJoin(role_).on(user_.roles.any().id.eq(role_.id))
                .leftJoin(menu_).on(menu_.roles.any().id.eq(role_.id))
                .where(user_.id.eq(user.getId()))
                .fetch();

        log.info("-----{}", fetch);
        return new LoginUserDetail(userDto, fetch);
    }
    
    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
    
}

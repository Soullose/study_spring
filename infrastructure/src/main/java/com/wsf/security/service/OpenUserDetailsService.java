package com.wsf.security.service;

import com.wsf.entity.User;
import com.wsf.repository.UserRepository;
import com.wsf.security.domain.LoginUserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * open
 * SoulLose
 * 2022-04-24 15:11
 */
@Service
@Slf4j
public class OpenUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        log.info("用户名：{}", userName);
        //查询用户信息
        User user = userRepository.getUserByUserName(userName);
        log.info("user: {}", user);
        if (Objects.isNull(user)) {
//            throw new UsernameNotFoundException(String.format("用户不存在 '%s'", userName));
            throw new RuntimeException(String.format("用户不存在 '%s'", userName));
        }
        //TODO 查询权限信息
        List<String> list = new ArrayList<>(Arrays.asList("test", "admin"));
        return new LoginUserDetail(user, list);
    }
}

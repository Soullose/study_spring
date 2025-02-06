package com.wsf.controller;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wsf.entity.QUser;
import com.wsf.infrastructure.security.domain.UserAccountDetail;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.wsf.entity.User;
import com.wsf.repository.UserRepository;

/**
 * open
 * SoulLose
 * 2022-05-30 09:52
 */
@Slf4j
@RequestMapping("test")
@RestController
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @GetMapping("/test")
    public ResponseEntity<?> test() {

        User user = new User();
        user.setIdCardNumber("333333333333333");
        user.setPhoneNumber("13333333333");
        user.setCreateTime(LocalDateTime.now());

        userRepository.save(user);
        log.info("test");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test1")
    public ResponseEntity<?> test1() {

//        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(manager);
        List<User> fetch = jpaQueryFactory.selectFrom(QUser.user).fetch();
        return ResponseEntity.ok(fetch);
    }

    @GetMapping("/employees")
    public String employee(){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        UserAccountDetail principal = (UserAccountDetail)authentication.getPrincipal();
        log.debug("当前用户:{}",principal);
        return "人员信息";
    }
}

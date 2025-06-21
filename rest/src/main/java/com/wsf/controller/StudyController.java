package com.wsf.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * open
 * SoulLose
 * 2022-04-21 15:47
 */
@Slf4j
@RequestMapping
@RestController
public class StudyController {

    @GetMapping("/hello")
//    @PreAuthorize("hasAuthority('hello:test')")
    public ResponseEntity<String> HelloWorld() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null || !authentication.isAuthenticated()) {
            log.debug("未登录");

            Object principal = authentication.getPrincipal();
            log.debug("UserAccountDetail:-{}",principal);
        }
        log.info("你好，世界！");
        return ResponseEntity.ok("你好，世界！");
    }
}

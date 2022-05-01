package com.wsf.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * open
 * SoulLose
 * 2022-04-21 15:47
 */
@RequestMapping
@RestController
public class StudyController {
    
    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('hello')")
    public String HelloWorld() {
        return "你好，世界！";
    }
    
    @GetMapping("/test")
    @PreAuthorize("hasAuthority('test')")
    public String test() {
        return "你好，测试！";
    }
}

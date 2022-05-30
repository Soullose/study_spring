package com.wsf.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * open
 * SoulLose
 * 2022-04-21 15:47
 */
@Slf4j
@RequestMapping
@RestController
public class StudyController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('hello:test')")
    public String HelloWorld() {
        return "你好，世界！";
    }
    
//    @GetMapping("/test")
////    @PreAuthorize("hasAuthority('test')")
//    public ResponseEntity<String> test() {
//        String forObject = restTemplate.getForObject("http://localhost:8080/employees", String.class);
//        log.debug("测试:{}",forObject);
//        return ResponseEntity.ok("你好，测试！");
//    }
//
//    @GetMapping("/employees")
//    public String employee(){
//        return "人员信息";
//    }
}

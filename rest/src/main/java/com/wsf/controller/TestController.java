package com.wsf.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
    private RestTemplate restTemplate;
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        String forObject = restTemplate.getForObject("http://localhost:8080/test/employees", String.class);
        log.debug("测试:{}",forObject);
        return ResponseEntity.ok("你好，测试！");
    }
    
    @GetMapping("/employees")
    public String employee(){
        return "人员信息";
    }
}

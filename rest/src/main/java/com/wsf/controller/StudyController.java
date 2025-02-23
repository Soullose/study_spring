package com.wsf.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        log.info("你好，世界！");
        return ResponseEntity.ok("你好，世界！");
    }
}

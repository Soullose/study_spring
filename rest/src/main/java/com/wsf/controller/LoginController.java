package com.wsf.controller;

import com.wsf.params.LoginUserParams;
import com.wsf.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * open
 * SoulLose
 * 2022-04-27 19:01
 */
@RequestMapping("/open")
@RestController
@Slf4j
public class LoginController {
    
    @Autowired
    private LoginService loginService;
    
    /**
     * 登录
     * @param loginUserParams   前端传过来的登录信息
     * @return                  {@link Map}
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserParams loginUserParams) {
        log.info("登录用户名密码：{}" ,loginUserParams);
        String jwt = loginService.login(loginUserParams);
        Map<String, Object> map = new HashMap<>();
        map.put("token",jwt);
        return ResponseEntity.ok(map);
    }
    
    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        loginService.logout();
        return ResponseEntity.ok().build();
    }
}

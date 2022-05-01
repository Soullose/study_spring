package com.wsf.service;


import com.wsf.params.LoginUserParams;

public interface LoginService {
    /**
     * 登录并返回jwt
     * @param loginUserParams   前端传过来的登录信息
     * @return                  {@link String}
     */
    String login(LoginUserParams loginUserParams);
    
    void logout();
}

package com.wsf.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * open
 * SoulLose
 * 2022-05-20 16:35
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    /**
     *
     * @param httpServletRequest    请求对象
     * @param response   响应对象
     * @param e                     异常对象
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {
        //处理异常
//        HashMap<String, String> map = new HashMap<>(2);
//        map.put("uri", httpServletRequest.getRequestURI());
//        map.put("msg", "认证失败");
        response.sendError(HttpStatus.UNAUTHORIZED.value(),"用户授权失败");
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        ObjectMapper objectMapper = new ObjectMapper();
//        String resBody = objectMapper.writeValueAsString(map);
//        PrintWriter printWriter = httpServletResponse.getWriter();
//        printWriter.print(resBody);
//        printWriter.flush();
//        printWriter.close();
    }
}

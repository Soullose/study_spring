package com.wsf.infrastructure.security.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户访问没有权限资源的处理
 * open
 * SoulLose
 * 2022-05-21 12:14
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);
    
    @Override
    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        //处理异常
//        HashMap<String, String> map = new HashMap<>(2);
//        map.put("uri", httpServletRequest.getRequestURI());
//        map.put("msg", "权限不足");
        response.sendError(HttpStatus.FORBIDDEN.value(),"权限不足");
//        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        ObjectMapper objectMapper = new ObjectMapper();
//        String resBody = objectMapper.writeValueAsString(map);
//        PrintWriter printWriter = response.getWriter();
//        printWriter.print(resBody);
//        printWriter.flush();
//        printWriter.close();
    }
}

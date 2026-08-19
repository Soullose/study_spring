package com.wsf.infrastructure.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 访问拒绝实现类
 */
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
	private static final Logger log = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		int status = HttpStatus.BAD_REQUEST.value();
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		try (PrintWriter writer = response.getWriter()) {
			writer.print("访问未授权");
			writer.flush(); // 确保将响应内容写入到输出流
		} catch (IOException e) {
			log.error("响应异常处理失败", e);
		}
	}
}
package com.wsf.infrastructure.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		String errorMsg = authException.getMessage();
		int status = HttpStatus.UNAUTHORIZED.value();
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		if (errorMsg != null && errorMsg.equals("Bad credentials")) {
			errorMsg = "用户名或密码错误";
		}
		try (PrintWriter writer = response.getWriter()) {
			writer.print(errorMsg);
			writer.flush(); // 确保将响应内容写入到输出流
		} catch (IOException e) {
			log.error("响应异常处理失败", e);
		}
	}
}

package com.wsf.infrastructure.security.handler;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.wsf.infrastructure.common.result.ResultCode;
import com.wsf.infrastructure.utils.ResponseUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 统一处理 Spring Security 认证失败响应
 */
@Slf4j
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		log.debug("request:{}", request);
		log.debug("请求URL: {} {}", request.getMethod(), request.getRequestURI());
		log.debug("当前认证信息: {}", SecurityContextHolder.getContext().getAuthentication());
		String errorMsg = authException.getMessage();
		log.error("AuthenticationEntryPointImpl:{}", errorMsg);
		if (authException instanceof BadCredentialsException) {
			/// 用户名或密码错误
			ResponseUtils.writeErrMsg(response, ResultCode.USER_PASSWORD_ERROR);
		} else if(authException instanceof InsufficientAuthenticationException){
			/// 请求头缺失Authorization、Token格式错误、Token过期、签名验证失败
			ResponseUtils.writeErrMsg(response, ResultCode.ACCESS_TOKEN_INVALID);
		} else {
			/// 其他未明确处理的认证异常（如账户被锁定、账户禁用等）
			ResponseUtils.writeErrMsg(response, ResultCode.USER_LOGIN_EXCEPTION, authException.getMessage());
		}
//		int status = HttpStatus.UNAUTHORIZED.value();
//		response.setStatus(status);
//		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//		if (authException instanceof UsernameNotFoundException) {
//			errorMsg = "用户名或密码错误";
//		} else if (authException instanceof JWTAuthException) {
//			errorMsg = "JWT异常";
//		}
//		try (PrintWriter writer = response.getWriter()) {
//			writer.print(errorMsg);
//			writer.flush(); // 确保将响应内容写入到输出流
//		} catch (IOException e) {
//			log.error("响应异常处理失败", e);
//		}
	}
}
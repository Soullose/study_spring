package com.wsf.infrastructure.security.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.wsf.infrastructure.common.result.ResultCode;
import com.wsf.infrastructure.utils.IpUtils;
import com.wsf.infrastructure.utils.ResponseUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationFailureHandlerImpl.class);

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		log.debug("ip:{}", IpUtils.getIpAddr(request));
		String message = authException.getMessage();
		log.error("authException:{} AuthenticationFailureHandlerImpl:{}", authException, message);
		if (authException instanceof LockedException) {
			ResponseUtils.writeErrMsg(response, ResultCode.USER_ACCOUNT_FROZEN, message);
		} else if (authException instanceof BadCredentialsException) {
			ResponseUtils.writeErrMsg(response, ResultCode.USER_PASSWORD_ERROR);
		} else if (authException instanceof InternalAuthenticationServiceException) {
			ResponseUtils.writeErrMsg(response, ResultCode.USER_ACCOUNT_FROZEN, message);
		} else {
			ResponseUtils.writeErrMsg(response, ResultCode.USER_PASSWORD_ERROR);
		}
	}
}

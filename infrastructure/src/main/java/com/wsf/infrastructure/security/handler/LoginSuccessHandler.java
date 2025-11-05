package com.wsf.infrastructure.security.handler;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.wsf.infrastructure.security.domain.UserAccountDetail;
import com.wsf.infrastructure.security.service.JwtService;
import com.wsf.infrastructure.utils.ResponseUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(LoginSuccessHandler.class);

	private final JwtService jwtService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		log.debug("登录成功");

		UserAccountDetail userAccountDetail = (UserAccountDetail) authentication.getPrincipal();

		String jwtToken = jwtService.generateToken(userAccountDetail);

		HashMap<String, String> result = new HashMap<>();
		result.put("accessToken", jwtToken);
		ResponseUtils.writeSuccessMsg(response, result);
	}
}

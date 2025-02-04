package com.wsf.infrastructure.security.handler;

import com.wsf.entity.Token;
import com.wsf.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class LogoutHandlerImpl implements LogoutHandler {

	private final TokenRepository tokenRepository;

	private static final String AUTHORIZATION = "authorization";

	@Override public void logout(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) {

		final String authHeader = request.getHeader(AUTHORIZATION);
		final String jwt;
		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			return;
		}
		jwt = authHeader.substring(7);
		Token storeToken = tokenRepository.findByToken(jwt)
				.orElse(null);
		if (storeToken != null) {
			storeToken.setExpired(true);
			storeToken.setRevoked(true);
			tokenRepository.save(storeToken);
		}
	}
}

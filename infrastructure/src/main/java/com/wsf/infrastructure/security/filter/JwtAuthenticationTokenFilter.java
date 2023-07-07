package com.wsf.infrastructure.security.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author soullose
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION = "authorization";

	@Override protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		final String authHeader = request.getHeader(AUTHORIZATION);

		if (authHeader == null){
			filterChain.doFilter(request,response);
		}
	}
}
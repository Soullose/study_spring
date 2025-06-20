package com.wsf.infrastructure.security.filter;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wsf.infrastructure.security.service.JwtService;
import com.wsf.infrastructure.security.service.OpenUserDetailsService;
import com.wsf.repository.TokenRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author soullose
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private final OpenUserDetailsService userDetailsService;

	private final TokenRepository tokenRepository;

	private static final String AUTHORIZATION = "authorization";

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		final String authHeader = request.getHeader(AUTHORIZATION);
		final String jwt;
		final String username;
		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		try {
			jwt = authHeader.substring(7);
			username = jwtService.extractUsername(jwt);
			log.debug("JwtAuthenticationTokenFilter-username:{}", username);
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
				/// 数据库校验token有效性
				Boolean isTokenValid = tokenRepository.findByToken(jwt).map(t -> !t.isRevoked() && !t.isExpired())
						.orElse(false);
				if (jwtService.isTokenValid(jwt, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					/// 添加其他详细信息到身份认证中如IP地址、会话ID或任何其他相关详细信息。
					/// 通过设置身份验证令牌的详细信息，您可以将这些信息提供给身份验证和授权过程的其他组件。
					/// 它允许下游组件（如身份验证提供者或访问决策管理器）在身份验证和授权过程中访问和使用这些附加细节。
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					/// 设置认证信息到 SecurityContext
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
			filterChain.doFilter(request, response);
		} catch (JwtException e) { // 捕获 JWT 库的原生异常（如 ExpiredJwtException、MalformedJwtException）
			log.warn("JWT 异常: {}", e.getMessage());
			handleJwtException(response, e);
		} catch (UsernameNotFoundException e) {
			log.warn("用户不存在: {}", e.getMessage());
			throw e;
		}
	}
	/**
	 * 直接处理JWT异常响应
	 */
	private void handleJwtException(HttpServletResponse response, JwtException e) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		String errorMessage = String.format("{\"error\":\"JWT认证失败\",\"message\":\"%s\"}",
				"无效的 JWT 令牌: " + e.getMessage());

		response.getWriter().write(errorMessage);
		response.getWriter().flush();
	}
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return request.getRequestURI().startsWith("/api/v1/auth/login");
	}
}
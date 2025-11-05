package com.wsf.infrastructure.security.filter;

import java.io.IOException;
import java.util.Collection;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wsf.infrastructure.security.repository.TokenRepository;
import com.wsf.infrastructure.security.service.JwtService;
import com.wsf.infrastructure.security.service.OpenUserDetailsService;

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
				Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
				
				// 首先检查JWT token本身的有效性
				if (jwtService.isTokenValid(jwt, userDetails)) {
					// 然后检查数据库中的token有效性（如果数据库中有记录）
					Boolean isTokenValidInDb = tokenRepository.findByToken(jwt)
							.map(t -> !t.isRevoked() && !t.isExpired())
							.orElse(true); // 如果数据库中找不到token记录，默认认为有效
					
					if (isTokenValidInDb) {
						UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
								null, authorities);
						/// 添加其他详细信息到身份认证中如IP地址、会话ID或任何其他相关详细信息。
						/// 通过设置身份验证令牌的详细信息，您可以将这些信息提供给身份验证和授权过程的其他组件。
						/// 它允许下游组件（如身份验证提供者或访问决策管理器）在身份验证和授权过程中访问和使用这些附加细节。
						authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

						/// 设置认证信息到 SecurityContext
						SecurityContextHolder.getContext().setAuthentication(authToken);
						log.debug("JwtAuthenticationTokenFilter: 成功设置认证信息到SecurityContext");
					} else {
						log.warn("JwtAuthenticationTokenFilter: 数据库中的token已失效或已撤销");
					}
				} else {
					log.warn("JwtAuthenticationTokenFilter: JWT token无效");
				}
			}
			filterChain.doFilter(request, response);
			
			// 在过滤器链执行后检查认证状态
			Authentication authAfter = SecurityContextHolder.getContext().getAuthentication();
			if (authAfter == null) {
				log.warn("JwtAuthenticationTokenFilter: 过滤器链执行后认证信息被清除");
			} else {
				log.debug("JwtAuthenticationTokenFilter: 过滤器链执行后认证信息仍然存在 - {}", authAfter.getName());
			}
		} catch (JwtException e) { // 捕获 JWT 库的原生异常（如 ExpiredJwtException、MalformedJwtException）
			log.warn("JWT 异常: {}", e.getMessage());
			// 安全上下文清除保障（防止上下文残留）
			SecurityContextHolder.clearContext();
			handleJwtException(response, e);
		} catch (UsernameNotFoundException e) {
			log.warn("用户不存在: {}", e.getMessage());
			// 安全上下文清除保障（防止上下文残留）
			SecurityContextHolder.clearContext();
			throw e;
		}

		// 继续后续过滤器链执行
		filterChain.doFilter(request, response);
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
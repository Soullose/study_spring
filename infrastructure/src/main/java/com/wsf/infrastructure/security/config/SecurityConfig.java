package com.wsf.infrastructure.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wsf.entity.QUser;
import com.wsf.entity.User;
import com.wsf.infrastructure.config.OpenPrimaryJpaConfig;
import com.wsf.infrastructure.security.filter.JwtAuthenticationTokenFilter;
import com.wsf.infrastructure.security.filter.LoginFilter;
import com.wsf.infrastructure.security.handler.AccessDeniedHandlerImpl;
import com.wsf.infrastructure.security.handler.AuthenticationEntryPointImpl;
import com.wsf.infrastructure.security.handler.LoginSuccessHandler;
import com.wsf.infrastructure.security.handler.LogoutHandlerImpl;
import com.wsf.infrastructure.security.service.JwtService;
import com.wsf.infrastructure.security.service.OpenUserDetailsService;
import com.wsf.infrastructure.utils.RedisUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@AutoConfigureAfter(value = OpenPrimaryJpaConfig.class)
public class SecurityConfig {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

	private final OpenUserDetailsService userDetailsService;

	private final LoginSuccessHandler loginSuccessHandler;

	private final LogoutHandlerImpl logoutHandler;

	private final JPAQueryFactory jpaQueryFactory;

	private final RedisUtil redisUtil;

	private final JwtService jwtService;

	// 在 SecurityConfiguration 中声明
	@Bean
	public RequestMatcher whiteListRequestMatcher() {
		return new OrRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/test/**"),
				PathPatternRequestMatcher.withDefaults().matcher("/api/v1/auth/**"),
				PathPatternRequestMatcher.withDefaults().matcher("/doc.html")
		// 其他白名单路径...
		);
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager)
			throws Exception {
		log.debug("配置SecurityFilterChain");
		/// 测试
		QUser qUser = QUser.user;
		User o = (User) jpaQueryFactory.from(qUser).fetchFirst();
		log.debug("o:{}", o);
		redisUtil.setStr("xxxx1", "222222222222222222222", 60000);
		http.authorizeHttpRequests((requests) -> requests
				// .antMatchers("/hello").permitAll()
				.requestMatchers("/doc.html", "/swagger-ui.html", "/api/doc.html", "/webjars/**", "/v3/**",
						"/swagger-resources/**")
				.permitAll().requestMatchers("/test/**").permitAll().requestMatchers("/api/v1/auth/**").permitAll()
				.anyRequest().authenticated())
				// .authenticationProvider(authenticationProvider())
				// .authenticationManager(authenticationManager)
				.exceptionHandling(configurer -> configurer.authenticationEntryPoint(new AuthenticationEntryPointImpl()) // 未认证异常处理器
						.accessDeniedHandler(new AccessDeniedHandlerImpl()) // 无权限访问异常处理器
				).sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				/// 禁用 CSRF 防护，前后端分离无需此防护机制
				.csrf(AbstractHttpConfigurer::disable)
				/// 禁用 HTTP Basic 认证，避免弹窗式登录
				.httpBasic(AbstractHttpConfigurer::disable)
				/// 禁用默认的表单登录功能，前后端分离采用 Token 认证方式
				.formLogin(AbstractHttpConfigurer::disable)
				/// 禁用 X-Frame-Options 响应头，允许页面被嵌套到 iframe 中
				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
				.addFilterAt(loginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
		// .rememberMe(remember -> {
		// remember.alwaysRemember(true);
		// remember.userDetailsService(userDetailsService);
		// remember.rememberMeParameter("rememberMe");
		// remember.rememberMeServices(rememberMeServices);
		// })
		;

		return http.build();

	}

	// @Bean
	// public RememberMeServices rememberMeServices() {
	// RedisTokenRepositoryImpl redisTokenRepository = new
	// RedisTokenRepositoryImpl(redisUtil);
	// return new
	// PersistentTokenBasedRememberMeServices(UUID.randomUUID().toString(),
	// userDetailsService, redisTokenRepository);
	// }

	// @Bean
	public LoginFilter loginFilter(AuthenticationManager authenticationManager) throws Exception {
		LoginFilter loginFilter = new LoginFilter();
		loginFilter.setAuthenticationManager(authenticationManager);
		// loginFilter.setRememberMeServices(rememberMeServices);
		loginFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
		return loginFilter;
	}

	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

	/// todo 添加其他provider

	@Bean
	AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
		return new ProviderManager(daoAuthenticationProvider);
	}

	/// 密码加密设置
	@Bean
	public PasswordEncoder passwordEncoder() {
		return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
		// return new BCryptPasswordEncoder();
	}

}
package com.wsf.infrastructure.security.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.wsf.entity.QUser;
import com.wsf.entity.User;
import com.wsf.infrastructure.config.OpenPrimaryJpaConfig;
import com.wsf.infrastructure.security.filter.JwtAuthenticationTokenFilter;
import com.wsf.infrastructure.security.handler.LogoutHandlerImpl;
import com.wsf.infrastructure.security.service.OpenUserDetailsService;
import com.wsf.infrastructure.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@AutoConfigureAfter(value = OpenPrimaryJpaConfig.class)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    // private AuthenticationProvider authenticationProvider;

    private final OpenUserDetailsService userDetailsService;

    private final LogoutHandlerImpl logoutHandler;

    private final JPAQueryFactory jpaQueryFactory;

    private final RedissonClient redissonClient;

    private final RedisUtil redisUtil;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.debug("配置SecurityFilterChain");
        /// 测试
        QUser qUser = QUser.user;
        User o = (User) jpaQueryFactory.from(qUser).fetchFirst();
        log.debug("o:{}", o);
        redisUtil.setStr("xxxx1", "222222222222222222222",60000);
        http
                .authorizeHttpRequests((requests) -> requests
                        // .antMatchers("/hello").permitAll()
                        .requestMatchers("/doc.html", "/swagger-ui.html", "/api/doc.html", "/webjars/**",
                                "/v3/**", "/swagger-resources/**").permitAll()
//                        .requestMatchers("/doc.html").permitAll()
//                        .requestMatchers("/swagger-ui.html").permitAll()
//                        .requestMatchers("/webjars/**").permitAll()
//                        .requestMatchers("/v3/**").permitAll()
//                        .requestMatchers("/swagger-resources/**").permitAll()
//                        .requestMatchers("/api/doc.html").permitAll()
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)

        ;

        return http.build();

    }


    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    /// todo 添加其他provider

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /// 密码加密设置
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
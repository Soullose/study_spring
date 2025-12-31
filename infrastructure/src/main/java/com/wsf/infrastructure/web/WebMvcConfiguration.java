package com.wsf.infrastructure.web;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * open
 * SoulLose
 * 2022-05-21 13:04
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

  private final DataSourceInterceptor dataSourceInterceptor;

  public WebMvcConfiguration(DataSourceInterceptor dataSourceInterceptor) {
    this.dataSourceInterceptor = dataSourceInterceptor;
  }

  @Override
  public void addInterceptors(@NotNull InterceptorRegistry registry) {
    registry
        .addInterceptor(dataSourceInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/doc.html", "/swagger-ui.html", "/api/doc.html", "/webjars/**", "/v3/**",
            "/swagger-resources/**", "/test/**", "/api/v1/auth/**"
        );
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        // .allowedOrigins("*")
        .allowedOriginPatterns("*")
        .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
        .maxAge(3600L)
        .allowCredentials(true);
  }
}

package com.wsf.infrastructure.web;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 数据源切换拦截器
 * 从请求头或参数中获取数据源名称并设置到上下文
 */
@Component
public class DataSourceInterceptor implements HandlerInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceInterceptor.class);

  // 数据源名称的参数名
  private static final String DATASOURCE_HEADER = "X-Data-Source";
  private static final String DATASOURCE_PARAM = "dataSource";

  @Override
  public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
    // 1. 优先从请求头获取数据源名称
    String dataSourceName = request.getHeader(DATASOURCE_HEADER);

    // 2. 如果没有从请求头获取到，尝试从请求参数获取
    if (dataSourceName == null || dataSourceName.trim().isEmpty()) {
      dataSourceName = request.getParameter(DATASOURCE_PARAM);
    }

    // 3. 如果获取到数据源名称，设置到上下文
    if (dataSourceName != null && !dataSourceName.trim().isEmpty()) {
      // DataSourceContextHolder.setDataSource(dataSourceName.trim());
      logger.debug("设置数据源: {}", dataSourceName);
    } else {
      // 如果没有指定数据源，使用默认数据源
      // DataSourceContextHolder.useDefaultDataSource();
      logger.debug("使用默认数据源");
    }

    return true;
  }

  @Override
  public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
      @NotNull Object handler, Exception ex) {
    // 请求完成后清除数据源上下文，防止内存泄漏
    // DataSourceContextHolder.clearDataSource();
    logger.debug("清除数据源上下文");
  }
}

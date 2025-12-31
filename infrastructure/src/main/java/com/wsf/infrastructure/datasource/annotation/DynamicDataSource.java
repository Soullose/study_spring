package com.wsf.infrastructure.datasource.annotation;

import java.lang.annotation.*;

/**
 * 动态数据源注解
 * 支持SpEL表达式，可以从方法参数、Spring Bean等获取数据源名称
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicDataSource {
  /**
   * 数据源名称，支持SpEL表达式
   * 例如：
   * 1. 固定值: "db2"
   * 2. 从参数获取: "#dataSourceName"
   * 3. 从对象属性获取: "#user.dataSource"
   * 4. 从Spring Bean获取: "@dataSourceService.getDefaultDataSource()"
   */
  String value() default "";

  /**
   * 是否强制使用指定的数据源（忽略拦截器设置的数据源）
   */
  boolean force() default false;
}

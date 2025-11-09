package com.wsf.infrastructure.eventbus.annotation;

import java.lang.annotation.*;

/**
 * 事件订阅注解
 * 用于标记事件处理方法，支持条件过滤
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventSubscribe {

    /**
     * 订阅的事件类型
     * 默认自动推断方法参数类型
     */
    Class<?> value() default void.class;

    /**
     * 处理顺序，数值越小优先级越高
     * 默认值为0
     */
    int order() default 0;

    /**
     * 条件表达式，使用SpEL表达式
     * 只有当表达式结果为true时才执行处理
     * 表达式可以访问事件对象，使用#event变量
     * 例如: "#event.user.id > 0"
     */
    String condition() default "";
}
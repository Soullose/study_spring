package com.wsf.infrastructure.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

/**
 * Spring 工具类：用于在非 Spring 管理的类中获取 Spring Bean 或上下文
 */
@Component
public class SpringUtil implements ApplicationContextAware {
	/// 静态变量保存 ApplicationContext（线程安全，Spring 单例）
	private static ApplicationContext applicationContext;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (SpringUtil.applicationContext == null) {
			SpringUtil.applicationContext = applicationContext;
		}
	}

	/**
	 * 获取 ApplicationContext
	 */
	public static ApplicationContext getApplicationContext() {
		if (applicationContext == null) {
			throw new IllegalStateException("ApplicationContext 未初始化，请检查 SpringUtil 是否被正确扫描为 Spring Bean");
		}
		return applicationContext;
	}

	/**
	 * 根据类型获取 Bean（单例）
	 * 
	 * @param requiredType
	 *            Bean 的类型（如 UserService.class）
	 * @param <T>
	 *            Bean 的泛型类型
	 * @return 对应类型的 Bean 实例
	 */
	public static <T> T getBean(Class<T> requiredType) {
		return getApplicationContext().getBean(requiredType);
	}

	/**
	 * 根据名称获取 Bean（需指定类型避免类型转换异常）
	 * 
	 * @param name
	 *            Bean 在 Spring 中的名称（默认是类名首字母小写，如 userService）
	 * @param <T>
	 *            Bean 的泛型类型
	 * @return 对应名称的 Bean 实例
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		return (T) getApplicationContext().getBean(name);
	}

	/**
	 * 根据名称和类型获取 Bean（推荐，避免类型转换错误）
	 * 
	 * @param name
	 *            Bean 的名称
	 * @param requiredType
	 *            Bean 的类型
	 * @param <T>
	 *            Bean 的泛型类型
	 * @return 对应名称和类型的 Bean 实例
	 */
	public static <T> T getBean(String name, Class<T> requiredType) {
		return getApplicationContext().getBean(name, requiredType);
	}

	/**
	 * 获取环境变量（如配置文件中的属性）
	 * 
	 * @param key
	 *            环境变量键（如 "spring.datasource.url"）
	 * @return 环境变量值
	 */
	public static String getEnv(String key) {
		return getApplicationContext().getEnvironment().getProperty(key);
	}

	public static String[] getBeanDefinitionNames() {
		return getApplicationContext().getBeanDefinitionNames();
	}

	public static boolean containsBean(String name) {
		return getApplicationContext().containsBean(name);
	}

	/**
	 * 发布 Spring 事件
	 * 
	 * @param event
	 *            Spring 事件
	 */
	public static void publishEvent(ApplicationEvent event) {
		getApplicationContext().publishEvent(event);
	}
}

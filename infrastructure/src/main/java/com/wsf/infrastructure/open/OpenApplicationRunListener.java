package com.wsf.infrastructure.open;

import java.time.Duration;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * open SoulLose 2022-05-26 20:58
 */
public class OpenApplicationRunListener implements SpringApplicationRunListener {
	private static final Logger log = LoggerFactory.getLogger(OpenApplicationRunListener.class);
	private Long startTime;

	public OpenApplicationRunListener(SpringApplication springApplication, String[] args) {
	}

	@Override
	public void starting(ConfigurableBootstrapContext bootstrapContext) {
		startTime = System.currentTimeMillis();
		log.debug("OpenApplicationRunListener启动开始 {}", LocalTime.now());
	}

	@Override
	public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
			ConfigurableEnvironment environment) {
		log.debug("OpenApplicationRunListener环境准备 准备耗时：{}毫秒", (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		log.debug("OpenApplicationRunListener上下文准备 耗时：{}毫秒", (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		log.debug("OpenApplicationRunListener上下文载入 耗时：{}毫秒", (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
	}

	@Override
	public void started(ConfigurableApplicationContext context, Duration timeTaken) {
		log.debug("服务启动Runner started");
	}

	@Override
	public void failed(ConfigurableApplicationContext context, Throwable exception) {
		log.debug("服务启动Runner failed");
	}
}

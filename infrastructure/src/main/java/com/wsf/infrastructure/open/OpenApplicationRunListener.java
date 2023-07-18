package com.wsf.infrastructure.open;

import com.wsf.infrastructure.vfs.VFSInitializer;
import com.wsf.infrastructure.vfs.VFSProtocolResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;

/**
 * open
 * SoulLose
 * 2022-05-26 20:58
 */
public class OpenApplicationRunListener implements SpringApplicationRunListener {
	private static final Logger log = LoggerFactory.getLogger(OpenApplicationRunListener.class);

	private  VFSInitializer vfs = new VFSInitializer();

//	private VFSProtocolResolver vfsProtocolResolver;

//	private final SpringApplication application;
//	private final String[] args;

	public OpenApplicationRunListener(SpringApplication springApplication, String[] args) {
//		this.application = springApplication,
//		this.args = args;
	}

	@Override
	public void starting(ConfigurableBootstrapContext bootstrapContext) {
		log.info("服务启动Runner starting");
	}

	@Override
	public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
			ConfigurableEnvironment environment) {
		log.debug("服务启动Runner environmentPrepared");
		vfs.init();
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		log.debug("服务启动Runner contextPrepared");
		context.addProtocolResolver(new VFSProtocolResolver());
	}

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		log.debug("服务启动Runner contextLoaded");
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

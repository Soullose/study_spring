package com.wsf.infrastructure.watchfile.commonsio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.wsf.infrastructure.watchfile.commonsio.event.WatchFileOnCreateEvent;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class WatchFileService extends FileAlterationListenerAdaptor {

	private static final Logger log = LoggerFactory.getLogger(WatchFileService.class);

	FileAlterationMonitor fileMonitor;

	/// 观察者模式
	private final ApplicationEventPublisher applicationEventPublisher;
	public WatchFileService(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	@PostConstruct
	private void init() throws IOException {
		log.info("===========Apache File Listener文件夹监听服务开启==========");
		Path watchPath = Paths.get(SystemUtils.getUserDir().getAbsolutePath()).resolve("watchfiles");
		/// 确保监听目录存在
		if (!Files.exists(watchPath)) {
			Files.createDirectories(watchPath);
			log.info("创建监听目录: {}", watchPath);
		}
		if (!Files.isDirectory(watchPath)) {
			throw new IllegalArgumentException("监听路径必须是目录: " + watchPath);
		}
		FileAlterationObserver fileAlterationObserver = FileAlterationObserver.builder().setFile(watchPath.toFile())
				.setFileFilter(new SuffixFileFilter("json", "zip")).setIOCase(IOCase.SYSTEM).get();
		fileAlterationObserver.addListener(this);
		/// 配置Monitor，第一个参数单位是毫秒，是监听的间隔；第二个参数就是绑定我们之前的观察对象。
		FileAlterationMonitor fileMonitor = new FileAlterationMonitor(1000, fileAlterationObserver);
		/// 启动开始监听
		try {
			fileMonitor.start();
		} catch (Exception e) {
			log.error("监听服务启动失败:{}", e.getLocalizedMessage());
			throw new RuntimeException(e);
		}
	}

	@PreDestroy
	private void stop() {
		log.info("===========Apache File Listener文件夹监听服务关闭==========");
		try {
			if (fileMonitor != null) {
				fileMonitor.stop();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onFileCreate(File file) {
		log.debug("File created Path: {}", file.getAbsolutePath());
		applicationEventPublisher.publishEvent(new WatchFileOnCreateEvent(this, file));
	}
}

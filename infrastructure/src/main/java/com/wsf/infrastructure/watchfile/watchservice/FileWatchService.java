package com.wsf.infrastructure.watchfile.watchservice;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.wsf.infrastructure.watchfile.watchservice.event.FileCreatedEvent;
import com.wsf.infrastructure.watchfile.watchservice.model.FileWatchProperties;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class FileWatchService {

	private static final Logger logger = LoggerFactory.getLogger(FileWatchService.class);

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private FileWatchProperties properties;

	private WatchService watchService;
	private final Map<WatchKey, Path> watchKeyPathMap = new HashMap<>();
	private ExecutorService executorService;
	private Future<?> watchTask;
	private volatile boolean running = false;

	@PostConstruct
	public void init() {
		if (!properties.isEnabled()) {
			logger.info("文件监听服务已禁用");
			return;
		}

		try {
			startWatching();
		} catch (IOException e) {
			logger.error("启动文件监听服务失败", e);
		}
	}

	public void startWatching() throws IOException {
		Path watchPath = Paths.get(SystemUtils.getUserDir().getAbsolutePath()).resolve(properties.getWatchPath());

		// 确保监听目录存在
		if (!Files.exists(watchPath)) {
			Files.createDirectories(watchPath);
			logger.info("创建监听目录: {}", watchPath);
		}

		if (!Files.isDirectory(watchPath)) {
			throw new IllegalArgumentException("监听路径必须是目录: " + watchPath);
		}

		watchService = FileSystems.getDefault().newWatchService();
		executorService = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "file-watch-thread");
			t.setDaemon(true);
			return t;
		});

		// 注册监听
		registerWatchService(watchPath);

		running = true;
		watchTask = executorService.submit(this::watchLoop);

		logger.info("文件监听服务已启动，监听路径: {}", watchPath);
	}

	private void registerWatchService(Path path) throws IOException {
		if (properties.isRecursive()) {
			// 递归注册所有子目录
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					registerDirectory(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			// 只注册根目录
			registerDirectory(path);
		}
	}

	private void registerDirectory(Path dir) throws IOException {
		WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

		watchKeyPathMap.put(key, dir);
		logger.debug("注册监听目录: {}", dir);
	}

	private void watchLoop() {
		while (running) {
			try {
				WatchKey key = watchService.take();
				Path dir = watchKeyPathMap.get(key);

				if (dir == null) {
					logger.warn("收到未知WatchKey的事件");
					key.cancel();
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					if (kind == StandardWatchEventKinds.OVERFLOW) {
						logger.warn("文件系统事件溢出");
						continue;
					}

					@SuppressWarnings("unchecked")
					WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
					Path fileName = pathEvent.context();
					Path fullPath = dir.resolve(fileName);

					handleFileEvent(kind, fullPath);

					// 如果是新创建的目录且启用了递归监听，需要注册该目录
					if (kind == StandardWatchEventKinds.ENTRY_CREATE && properties.isRecursive()
							&& Files.isDirectory(fullPath)) {
						try {
							registerDirectory(fullPath);
						} catch (IOException e) {
							logger.error("注册新目录监听失败: {}", fullPath, e);
						}
					}
				}

				boolean valid = key.reset();
				if (!valid) {
					watchKeyPathMap.remove(key);
					logger.warn("WatchKey失效，移除监听: {}", dir);

					if (watchKeyPathMap.isEmpty()) {
						logger.warn("所有监听已失效，停止监听服务");
						break;
					}
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.info("文件监听线程被中断");
				break;
			} catch (Exception e) {
				logger.error("文件监听过程中发生异常", e);
			}
		}
	}

	private void handleFileEvent(WatchEvent.Kind<?> kind, Path filePath) {
		try {
			if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
				logger.info("文件创建: {}", filePath);
				eventPublisher.publishEvent(new FileCreatedEvent(this, filePath));

			} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
				logger.info("文件修改: {}", filePath);
				// eventPublisher.publishEvent(new FileModifiedEvent(this, filePath));

			} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
				logger.info("文件删除: {}", filePath);
				// eventPublisher.publishEvent(new FileDeletedEvent(this, filePath));
			}
		} catch (Exception e) {
			logger.error("处理文件事件失败: {} - {}", kind, filePath, e);
		}
	}

	@PreDestroy
	public void stop() {
		running = false;

		if (watchTask != null && !watchTask.isDone()) {
			watchTask.cancel(true);
		}

		if (executorService != null) {
			executorService.shutdown();
		}

		if (watchService != null) {
			try {
				watchService.close();
			} catch (IOException e) {
				logger.error("关闭WatchService失败", e);
			}
		}

		watchKeyPathMap.clear();
		logger.info("文件监听服务已停止");
	}

}

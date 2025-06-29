package com.wsf.infrastructure.watchfile.commonsio;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.wsf.infrastructure.watchfile.commonsio.event.WatchFileOnCreateEvent;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class WatchFileEvents {
	private static final Logger log = LoggerFactory.getLogger(WatchFileEvents.class);

	@EventListener
	private void fileOnCreate(WatchFileOnCreateEvent event) {
		File file = event.getFile();
		if (file.isFile()) {
			log.debug("新文件:{}", event.getFile().getName());
		}
	}
}

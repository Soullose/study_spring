package com.wsf.infrastructure.watchfile.watchservice.event;

import java.nio.file.Path;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class FileCreatedEvent extends ApplicationEvent {
	private Path filePath;

	public FileCreatedEvent(Object source) {
		super(source);
	}
	public FileCreatedEvent(Object source, Path filePath) {
		super(source);
		this.filePath = filePath;
	}
}
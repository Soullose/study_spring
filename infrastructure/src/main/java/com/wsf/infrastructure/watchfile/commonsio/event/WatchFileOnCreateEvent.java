package com.wsf.infrastructure.watchfile.commonsio.event;

import java.io.File;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class WatchFileOnCreateEvent extends ApplicationEvent {
    private final File file;
    public WatchFileOnCreateEvent(Object source, File file) {
        super(source);
        this.file = file;
    }
}

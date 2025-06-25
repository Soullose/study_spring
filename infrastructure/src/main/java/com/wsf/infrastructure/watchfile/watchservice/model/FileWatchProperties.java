package com.wsf.infrastructure.watchfile.watchservice.model;

import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
public class FileWatchProperties {
    private String watchPath = "watch";
    private boolean recursive = true;
    private boolean enabled = true;
}

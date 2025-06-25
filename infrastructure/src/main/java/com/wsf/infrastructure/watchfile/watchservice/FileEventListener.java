package com.wsf.infrastructure.watchfile.watchservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.wsf.infrastructure.watchfile.watchservice.event.FileCreatedEvent;

@Component
public class FileEventListener {
    private static final Logger logger = LoggerFactory.getLogger(FileEventListener.class);

//    @Async
    @EventListener
    public void handleFileCreated(FileCreatedEvent event) {
        logger.info("处理文件创建事件: {} at {}",
                event.getFilePath(), event.getTimestamp());

        // 在这里添加你的业务逻辑
        // 例如：文件备份、数据处理、通知发送等
//        processCreatedFile(event.getFilePath());
    }
}

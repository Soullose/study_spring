package com.wsf.infrastructure.vfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.ProtocolResolver;

public class ApacheVfsApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private final static Logger log = LoggerFactory.getLogger(ApacheVfsApplicationContextInitializer.class);

    /**
     * 协议解析器
     */
    public ProtocolResolver newProtocolResolver() {
        return new ApacheVfsProtocolResolver();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ApacheVfsInitializer.getInstance().init();
        if (log.isDebugEnabled()) {
            log.debug("ApacheVfsProtocolResolver Start");
        }
        // 添加虚拟文件协议分解器
        applicationContext.addProtocolResolver(newProtocolResolver());
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}

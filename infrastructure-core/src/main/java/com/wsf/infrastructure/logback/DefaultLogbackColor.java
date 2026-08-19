package com.wsf.infrastructure.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

/**
 * open
 * 2022/5/18
 */
public class DefaultLogbackColor extends ForegroundCompositeConverterBase<ILoggingEvent> {
    public DefaultLogbackColor() {}
    @Override
    protected String getForegroundColorCode(ILoggingEvent iLoggingEvent) {
        Level level = iLoggingEvent.getLevel();
        switch(level.toInt()) {
            case 10000:
                return "1;35";
            case 20000:
                return "39";
            case 30000:
                return "1;33";
            case 40000:
                return "1;31";
            default:
                return "39";
        }
    }
}

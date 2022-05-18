package com.wsf.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;


public class CustomLogbackColor extends ForegroundCompositeConverterBase<ILoggingEvent> {

    public CustomLogbackColor() {}

    @Override
    protected String getForegroundColorCode(ILoggingEvent iLoggingEvent) {
        Level level = iLoggingEvent.getLevel();
        switch(level.toInt()) {
            case 10000:
                return "1;35";
            case 20000:
                return "32";
            case 30000:
                return "1;33";
            case 40000:
                return "1;31";
            default:
                return "39";
        }
    }
}

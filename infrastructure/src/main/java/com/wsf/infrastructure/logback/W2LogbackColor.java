package com.wsf.infrastructure.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class W2LogbackColor extends ForegroundCompositeConverterBase<ILoggingEvent> {
	@Override
	protected String getForegroundColorCode(ILoggingEvent event) {
		Level level = event.getLevel();
		switch (level.toInt()) {
			// ERROR等级为红色
			case Level.ERROR_INT :
				return ANSIConstants.BOLD + ANSIConstants.RED_FG;
			// WARN等级为黄色
			case Level.WARN_INT :
				return ANSIConstants.BOLD + ANSIConstants.YELLOW_FG;
			// INFO等级为黑色
			case Level.INFO_INT :
				return ANSIConstants.GREEN_FG;
			// DEBUG等级为紫色
			case Level.DEBUG_INT :
				return ANSIConstants.BOLD + ANSIConstants.MAGENTA_FG;
			// 其他为默认颜色
			default :
				return ANSIConstants.DEFAULT_FG;
		}
	}
}

package com.wsf.domain.event;

/**
 * 用户领域事件标记接口。
 * <p>
 * 实现此接口的事件属于"用户域"，可通过 {@code @EventListener(UserDomainEventType.class)}
 * 统一监听所有用户域事件，或通过 {@code @Async("eventUserExecutor")} 投递到用户域专用线程池。
 * </p>
 *
 * @author wsf
 * @since 1.0
 */
public interface UserDomainEventType {
}

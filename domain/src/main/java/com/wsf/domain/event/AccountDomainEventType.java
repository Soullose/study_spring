package com.wsf.domain.event;

/**
 * 账户领域事件标记接口。
 * <p>
 * 实现此接口的事件属于"账户域"，可通过 {@code @EventListener(AccountDomainEventType.class)}
 * 统一监听所有账户域事件，或通过 {@code @Async("eventAccountExecutor")} 投递到账户域专用线程池。
 * </p>
 *
 * @author wsf
 * @since 1.0
 */
public interface AccountDomainEventType {
}

package com.wsf.infrastructure.config;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;

/**
 * Spring Event 异步配置。
 * <p>
 * 提供：
 * <ul>
 *   <li>{@code eventUserExecutor} —— 用户域事件专用线程池</li>
 *   <li>{@code eventAccountExecutor} —— 账户域事件专用线程池</li>
 *   <li>{@code eventSystemExecutor} —— 系统域事件（如角色分配）专用线程池</li>
 *   <li>{@code applicationEventMulticaster} —— 自定义事件广播器（同步 + 错误处理）</li>
 * </ul>
 * 通过 {@code @Async("eventUserExecutor")} 在不同监听器上指定不同线程池，
 * 实现事件按领域的物理隔离，避免某一领域的大量事件阻塞其他领域。
 * </p>
 *
 * @author wsf
 * @since 1.0
 */
@Configuration
@EnableAsync
public class SpringEventConfiguration implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SpringEventConfiguration.class);

    /** CPU 核数 */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 默认异步线程池（兜底）。
     * <p>
     * 当 {@code @Async} 未指定 value 时使用此线程池。
     * </p>
     */
    @Override
    public Executor getAsyncExecutor() {
        return eventSystemExecutor();
    }

    /**
     * 异步任务异常处理器。
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) ->
                log.error("Async method [{}] threw uncaught exception", method.getName(), ex);
    }

    // ======================== 领域线程池 ========================

    /**
     * 用户域事件线程池。
     * <ul>
     *   <li>核心线程数 = CPU 核数（IO 密集型可适当放大）</li>
     *   <li>最大线程数 = CPU 核数 × 2</li>
     *   <li>队列容量 200</li>
     *   <li>拒绝策略：CallerRunsPolicy（让调用线程执行，防止丢事件）</li>
     * </ul>
     */
    @Bean("eventUserExecutor")
    public Executor eventUserExecutor() {
        return buildExecutor("event-user-", CPU_COUNT, CPU_COUNT * 2, 200);
    }

    /**
     * 账户域事件线程池。
     */
    @Bean("eventAccountExecutor")
    public Executor eventAccountExecutor() {
        return buildExecutor("event-account-", CPU_COUNT / 2, CPU_COUNT, 100);
    }

    /**
     * 系统域事件线程池（角色分配等跨领域事件）。
     */
    @Bean("eventSystemExecutor")
    public Executor eventSystemExecutor() {
        return buildExecutor("event-system-", Math.max(2, CPU_COUNT / 2), CPU_COUNT, 100);
    }

    // ======================== 自定义广播器 ========================

    /**
     * 自定义 ApplicationEventMulticaster。
     * <p>
     * 使用自定义线程池执行同步监听器（当监听器未标注 {@code @Async} 时使用此线程池），
     * 并设置错误处理器，避免单个监听器异常影响其他监听器。
     * </p>
     */
    @Bean(name = "applicationEventMulticaster")
    public SimpleApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();

        // 同步监听器也使用线程池（不阻塞发布线程）
        ThreadPoolTaskExecutor syncExecutor = buildExecutor("event-sync-", 2, 4, 50);
        syncExecutor.initialize();
        multicaster.setTaskExecutor(syncExecutor);

        // 错误处理器：单个监听器异常不中断后续监听器
        multicaster.setErrorHandler(t -> log.error("Event listener execution failed", t));

        return multicaster;
    }

    // ======================== 构建工具方法 ========================

    /**
     * 构建标准线程池。
     *
     * @param prefix       线程名前缀
     * @param coreSize     核心线程数
     * @param maxSize      最大线程数
     * @param queueCapacity 阻塞队列容量
     * @return 配置完成的 ThreadPoolTaskExecutor
     */
    private ThreadPoolTaskExecutor buildExecutor(String prefix, int coreSize, int maxSize, int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setKeepAliveSeconds(60);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(prefix);
        // 拒绝策略：调用者线程执行，防止事件丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // TaskDecorator：传递 SecurityContext 到子线程
        executor.setTaskDecorator(new SecurityContextTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * TaskDecorator：确保 Spring Security 上下文和 MDC 在异步线程中可用。
     */
    private static class SecurityContextTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // 传递 SecurityContext
            Runnable decorated = new DelegatingSecurityContextRunnable(runnable);
            // 可选：传递 MDC 上下文（用于日志追踪）
            return decorated;
        }
    }
}

package com.wsf.test;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wsf.jpa.id.snowflake.SnowflakeIdGenerator;

public class SnowflakeConcurrencyTest {// 测试参数配置
    private static final int THREAD_COUNT = 128;     // 并发线程数
    private static final int GENERATIONS_PER_THREAD = 10_000; // 每个线程生成数量
    private static final long TEST_EPOCH = Instant.parse("2025-01-01T00:00:00Z").toEpochMilli();

    // 线程安全的ID容器
    private final Set<Long> idSet = Collections.synchronizedSet(new HashSet<>());
    private final SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1, TEST_EPOCH);

    public void runConcurrencyTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT,
                new ThreadFactoryBuilder().setNameFormat("id-generator-%d").build());

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);

        Stopwatch watch = Stopwatch.createUnstarted();

        // 提交任务
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < GENERATIONS_PER_THREAD; j++) {
                        long id = generator.nextId();
                        idSet.add(id);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 开始测试
        watch.start();
        startLatch.countDown();
        endLatch.await();
        watch.stop();

        executor.shutdownNow();

        // 结果验证
        int expectedCount = THREAD_COUNT * GENERATIONS_PER_THREAD;
        int actualCount = idSet.size();

        System.out.println("\n=== 并发测试结果 ===");
        System.out.println("线程数: " + THREAD_COUNT);
        System.out.println("总生成量: " + expectedCount);
        System.out.println("唯一ID数: " + actualCount);
        System.out.println("重复数量: " + (expectedCount - actualCount));
        System.out.println("耗时: " + watch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        System.out.println("QPS: " + (expectedCount * 1000L / watch.elapsed(TimeUnit.MILLISECONDS)));
    }

    public static void main(String[] args) throws InterruptedException {
        new SnowflakeConcurrencyTest().runConcurrencyTest();
    }
}

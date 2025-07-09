package com.wsf.infrastructure.modbus.test;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.wsf.infrastructure.modbus.client.ModbusClient;

public class DeviceMonitorTask implements Runnable {
    private final String deviceId;

    public DeviceMonitorTask(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void run() {
        // 这里实现实际的设备监控逻辑
        System.out.printf("[%s] 设备 %s 监控中...%n",
                Thread.currentThread().getName(), deviceId);
        ModbusClient client = new ModbusClient();

        try {
            // 连接到Modbus服务器
            client.connect("127.0.0.1", 502).get();
            System.out.println("连接成功");

            // 读取Holding Registers
            // 从地址0开始读取10个寄存器，设备ID为1
            int[] values = client.readHoldingRegisters(1, 0, 10).get();
            System.out.println("读取的寄存器值:");
            for (int i = 0; i < values.length; i++) {
                System.out.printf("地址 %d: %d (0x%04X)\n", i, values[i], values[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 断开连接
            try {
                client.disconnect().get();
                System.out.println("连接已断开");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

// 设备线程池管理器
class DeviceThreadPoolManager {
    private final Map<String, ScheduledExecutorService> deviceSchedulers = new ConcurrentHashMap<>();
    private final Map<String, ExecutorService> deviceThreadPools = new ConcurrentHashMap<>();
    private final ScheduledExecutorService globalScheduler =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    // 添加设备并启动监控
    public void addDevice(String deviceId, long periodMillis) {
        // 为每个设备创建专用线程池（固定2线程）
        ExecutorService threadPool = Executors.newFixedThreadPool(2,
                new DeviceThreadFactory(deviceId));

        // 创建设备专属调度器
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
                new DeviceThreadFactory(deviceId + "-Scheduler"));

        deviceThreadPools.put(deviceId, threadPool);
        deviceSchedulers.put(deviceId, scheduler);

        // 启动定时任务
        scheduler.scheduleAtFixedRate(() -> {
            // 每次调度提交两个任务到设备线程池
            threadPool.submit(new DeviceMonitorTask(deviceId));
            threadPool.submit(new DeviceMonitorTask(deviceId));
        }, 0, periodMillis, TimeUnit.MILLISECONDS);
    }

    // 移除设备并释放资源
    public void removeDevice(String deviceId) {
        ScheduledExecutorService scheduler = deviceSchedulers.remove(deviceId);
        ExecutorService threadPool = deviceThreadPools.remove(deviceId);

        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
    }

    // 全局关闭
    public void shutdown() {
        deviceSchedulers.values().forEach(ScheduledExecutorService::shutdownNow);
        deviceThreadPools.values().forEach(ExecutorService::shutdownNow);
        globalScheduler.shutdownNow();
    }

    // 自定义线程工厂（含设备标识）
    private static class DeviceThreadFactory implements ThreadFactory {
        private final String devicePrefix;
        private final AtomicInteger counter = new AtomicInteger(1);

        DeviceThreadFactory(String deviceId) {
            this.devicePrefix = "DevPool-" + deviceId + "-Thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, devicePrefix + counter.getAndIncrement());
        }
    }
}

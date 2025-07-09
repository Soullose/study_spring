package com.wsf.infrastructure.modbus.test;

public class DeviceMonitoringSystem {
    public static void main(String[] args) {
        DeviceThreadPoolManager manager = new DeviceThreadPoolManager();

        // 模拟添加50个设备（示例频率：设备1=1秒，设备2=2秒...）
        for (int i = 1; i <= 1; i++) {
            manager.addDevice("Device-" + i, i * 1000L);
        }

        // 注册JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭所有设备监控...");
            manager.shutdown();
            System.out.println("资源释放完成");
        }));

        System.out.println("设备监控系统已启动");
    }
}

package com.wsf.infrastructure.jpa.id.snowflake;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;

public class SnowflakeIdGenerator {
	// 各部分位数分配
	private static final int TIMESTAMP_BITS = 41;
	private static final int DATACENTER_BITS = 5;
	private static final int WORKER_BITS = 5;
	private static final int SEQUENCE_BITS = 12;

	// 最大值计算
	private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_BITS);
	private static final long MAX_WORKER_ID = ~(-1L << WORKER_BITS);
	private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

	// 位移计算
	private static final int WORKER_SHIFT = SEQUENCE_BITS;
	private static final int DATACENTER_SHIFT = SEQUENCE_BITS + WORKER_BITS;
	private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_BITS + DATACENTER_BITS;

	private final long datacenterId;
	private final long workerId;
	private final long epoch;

	private long sequence = 0L;
	private long lastTimestamp = -1L;

	private long MAX_BACKWARD_MS = 100L;

	private final Lock lock = new ReentrantLock();
	/**
	 * 构造函数
	 * 
	 * @param datacenterId
	 *            数据中心ID (0-31)
	 * @param workerId
	 *            工作节点ID (0-31)
	 * @param epoch
	 *            起始时间戳（毫秒）
	 */
	public SnowflakeIdGenerator(long datacenterId, long workerId, long epoch) {
		Preconditions.checkArgument(datacenterId >= 0 && datacenterId <= MAX_DATACENTER_ID,
				"Datacenter ID must be between 0 and %s", MAX_DATACENTER_ID);
		Preconditions.checkArgument(workerId >= 0 && workerId <= MAX_WORKER_ID, "Worker ID must be between 0 and %s",
				MAX_WORKER_ID);
		Preconditions.checkArgument(epoch <= System.currentTimeMillis(), "Epoch cannot be in the future");

		this.datacenterId = datacenterId;
		this.workerId = workerId;
		this.epoch = epoch;
	}

	/**
	 * 生成下一个ID
	 */
	public synchronized long nextId() {
		lock.lock();
		try {
			long currentTimestamp = timeGen();

			// 处理时钟回拨
			if (currentTimestamp < lastTimestamp) {
				long offset = lastTimestamp - currentTimestamp;
				if (offset <= MAX_BACKWARD_MS) {
					// 等待而不是直接抛出异常
					TimeUnit.MILLISECONDS.sleep(offset);
					currentTimestamp = timeGen();
				}else {
					throw new IllegalStateException(
							String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
									lastTimestamp - currentTimestamp));
				}
			}

			if (currentTimestamp == lastTimestamp) {
				sequence = (sequence + 1) & MAX_SEQUENCE;
				if (sequence == 0) {
					currentTimestamp = tilNextMillis(lastTimestamp);
				}
			} else {
				sequence = 0L;
			}

			lastTimestamp = currentTimestamp;

			return ((currentTimestamp - epoch) << TIMESTAMP_SHIFT) | (datacenterId << DATACENTER_SHIFT)
					| (workerId << WORKER_SHIFT) | sequence;
		}
		catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
		finally {
			lock.unlock();
		}
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	private long timeGen() {
		return System.currentTimeMillis();
	}

	// 以下是工具方法和示例用法
	public static Date parseIdTimestamp(long id, long epoch) {
		long timestamp = (id >> TIMESTAMP_SHIFT) + epoch;
		return new Date(timestamp);
	}

	public static void main(String[] args) {
		// 示例：使用2023-01-01作为epoch
		// DateUtils
		long customEpoch = Instant.parse("2025-01-01T00:00:00Z").toEpochMilli();

		SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1, customEpoch);

		// 生成10个ID
		for (int i = 0; i < 10; i++) {
			long id = generator.nextId();
			System.out.println("Generated ID: " + id);
			System.out.println("Parsed timestamp: " + parseIdTimestamp(id, customEpoch));
		}
	}
}

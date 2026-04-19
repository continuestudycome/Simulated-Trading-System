package com.lzq.simulatedtradingsystem.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnowflakeIdGenerator {

    // 起始时间戳（2024-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1776556800000L;

    // 机器ID所占位数
    private static final long MACHINE_ID_BITS = 5L;

    // 数据中心ID所占位数
    private static final long DATA_CENTER_ID_BITS = 5L;

    // 序列号所占位数
    private static final long SEQUENCE_BITS = 12L;

    // 最大值计算
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 位移量
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATA_CENTER_ID_BITS;

    private final long machineId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long machineId, long dataCenterId) {
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("机器ID范围必须在0-" + MAX_MACHINE_ID);
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("数据中心ID范围必须在0-" + MAX_DATA_CENTER_ID);
        }
        this.machineId = machineId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            log.error("时钟回拨异常，拒绝生成ID");
            throw new RuntimeException("时钟回拨异常");
        }

        // 同一毫秒内，序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为0
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装ID：时间戳部分 | 数据中心ID | 机器ID | 序列号
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
}

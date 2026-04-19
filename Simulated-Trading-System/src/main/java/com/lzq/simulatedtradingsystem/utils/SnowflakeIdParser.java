package com.lzq.simulatedtradingsystem.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SnowflakeIdParser {

    private static final long START_TIMESTAMP = 1776556800000L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MACHINE_ID_BITS = 5L;
    private static final long DATA_CENTER_ID_BITS = 5L;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 解析雪花ID
     */
    public static void parseId(long id) {
        System.out.println("========== 雪花ID解析 ==========");
        System.out.println("原始ID: " + id);
        System.out.println("二进制: " + Long.toBinaryString(id));

        // 提取序列号
        long sequence = id & MAX_SEQUENCE;

        // 提取机器ID
        long machineId = (id >> MACHINE_ID_SHIFT) & MAX_MACHINE_ID;

        // 提取数据中心ID
        long dataCenterId = (id >> DATA_CENTER_ID_SHIFT) & MAX_DATA_CENTER_ID;

        // 提取时间戳
        long timestamp = (id >> TIMESTAMP_SHIFT) + START_TIMESTAMP;

        System.out.println("\n【组成部分】");
        System.out.println("时间戳: " + timestamp + " ms");
        System.out.println("生成时间: " + formatTime(timestamp));
        System.out.println("数据中心ID: " + dataCenterId);
        System.out.println("机器ID: " + machineId);
        System.out.println("序列号: " + sequence);

        System.out.println("\n【验证计算】");
        long calculatedId = ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
        System.out.println("重新计算的ID: " + calculatedId);
        System.out.println("是否匹配: " + (calculatedId == id));
        System.out.println("==============================");
    }

    private static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date(timestamp));
    }

    public static void main(String[] args) {
        parseId(118495593500672L);
    }
}

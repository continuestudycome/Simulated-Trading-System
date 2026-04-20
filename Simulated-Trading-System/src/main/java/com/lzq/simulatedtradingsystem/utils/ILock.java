package com.lzq.simulatedtradingsystem.utils;

import java.util.concurrent.TimeUnit;

public interface ILock {
    
    /**
     * 获取锁
     */
    void lock();
    
    /**
     * 获取锁，支持超时
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否成功获取锁
     */
    boolean tryLock(long timeout, TimeUnit unit);
    
    /**
     * 释放锁
     */
    void unlock();
}

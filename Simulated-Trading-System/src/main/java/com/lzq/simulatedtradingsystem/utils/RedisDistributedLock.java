package com.lzq.simulatedtradingsystem.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis原生命令的分布式锁实现
 * 使用SET NX EX命令实现加锁，Lua脚本实现安全解锁
 * 适用于模拟交易系统中的并发控制场景，如账户余额扣减、持仓更新等
 */
@Slf4j
public class RedisDistributedLock implements ILock {
    
    /**
     * RedisTemplate实例，用于执行Redis命令
     */
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 锁的唯一标识key
     * 格式建议："lock:business:id"，例如："lock:user:123"
     */
    private final String lockKey;
    
    /**
     * 线程唯一标识，用于标识锁的持有者
     * 使用UUID确保全局唯一性，避免不同JVM中的线程ID冲突
     */
    private final String threadId;
    
    /**
     * 默认锁持有时间（秒）
     * 防止业务异常导致锁无法释放，造成死锁
     */
    private static final long DEFAULT_LEASE_TIME = 10L;
    
    /**
     * 默认等待超时时间（秒）
     * tryLock时的最大等待时间
     */
    private static final long DEFAULT_WAIT_TIMEOUT = 5L;
    
    /**
     * 自旋间隔时间（毫秒）
     * 尝试获取锁时的休眠时间，避免频繁请求Redis
     */
    private static final long SPIN_INTERVAL_MS = 100L;
    
    /**
     * 释放锁的Lua脚本
     * 保证判断和删除操作的原子性
     */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    
    static {
        try {
            ClassPathResource resource = new ClassPathResource("unlock.lua");
            String scriptContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            UNLOCK_SCRIPT = new DefaultRedisScript<>(scriptContent, Long.class);
        } catch (IOException e) {
            log.error("加载unlock.lua脚本失败", e);
            throw new RuntimeException("加载unlock.lua脚本失败", e);
        }
    }
    
    /**
     * 构造函数
     * @param redisTemplate RedisTemplate实例，由Spring容器注入
     * @param lockKey 锁的唯一标识key
     */
    public RedisDistributedLock(RedisTemplate<String, Object> redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.threadId = UUID.randomUUID().toString() + ":" + Thread.currentThread().getId();
    }
    
    /**
     * 阻塞式获取锁
     * 使用Redis的SET key value NX EX seconds命令实现
     * NX表示只在key不存在时设置，EX表示设置过期时间
     * 
     * 实现原理：
     * 1. 尝试设置锁，如果key不存在则设置成功并返回true
     * 2. 如果key已存在（锁被占用），则每隔100ms重试一次
     * 3. 直到获取锁成功
     * 
     * 使用场景：
     * - 必须执行的业务逻辑，不允许跳过
     * - 对响应时间要求不高的场景
     * 
     * 注意：
     * - 该方法会阻塞当前线程
     * - 建议在try-finally块中使用，确保锁能被正确释放
     */
    @Override
    public void lock() {
        while (true) {
            // 尝试获取锁，设置过期时间防止死锁
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, threadId, DEFAULT_LEASE_TIME, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("获取分布式锁成功，lockKey: {}, threadId: {}", lockKey, threadId);
                return;
            }
            
            // 获取锁失败，短暂休眠后重试
            try {
                Thread.sleep(SPIN_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待获取锁时被中断，lockKey: {}", lockKey, e);
                throw new RuntimeException("等待获取锁时被中断", e);
            }
        }
    }
    
    /**
     * 尝试获取锁（非阻塞）
     * 在指定时间内尝试获取锁，如果获取成功返回true，否则返回false
     * 
     * @param timeout 等待超时时间
     * @param unit 时间单位
     * @return true-成功获取锁，false-获取失败（超时或被中断）
     * 
     * 实现原理：
     * 1. 计算截止时间
     * 2. 在截止时间前不断尝试获取锁
     * 3. 每次尝试失败后休眠100ms
     * 4. 超时后返回false
     * 
     * 使用场景：
     * - 需要快速失败的业务场景
     * - 有降级策略的场景（获取锁失败可以执行备选方案）
     * - 对用户响应时间敏感的操作
     */
    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        long endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        
        while (System.currentTimeMillis() < endTime) {
            // 尝试获取锁
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, threadId, DEFAULT_LEASE_TIME, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("获取分布式锁成功，lockKey: {}, threadId: {}", lockKey, threadId);
                return true;
            }
            
            // 获取锁失败，短暂休眠后重试
            try {
                Thread.sleep(SPIN_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待获取锁时被中断，lockKey: {}", lockKey, e);
                return false;
            }
        }
        
        log.warn("获取分布式锁超时，lockKey: {}", lockKey);
        return false;
    }
    
    /**
     * 释放分布式锁
     * 使用Lua脚本保证释放锁操作的原子性
     * 
     * Lua脚本执行流程：
     * 1. 获取锁中存储的线程ID（GET key）
     * 2. 比较当前线程ID与锁中存储的线程ID是否一致
     * 3. 如果一致，则删除key释放锁（DEL key）
     * 4. 如果不一致，不做任何操作，返回nil
     * 
     * 为什么使用Lua脚本？
     * - 保证判断和删除操作的原子性，避免并发问题
     * - 防止误删其他线程的锁
     * - 减少网络开销，多个命令在一次请求中完成
     * 
     * 注意：
     * - 必须在finally块中调用，确保锁一定会被释放
     * - 不要重复释放锁
     */
    @Override
    public void unlock() {
        try {
            // 执行Lua脚本释放锁
            Long result = redisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(lockKey),
                threadId
            );
            
            if (result != null && result == 1L) {
                log.debug("分布式锁释放成功，lockKey: {}, threadId: {}", lockKey, threadId);
            } else {
                log.warn("分布式锁释放失败，可能锁已过期或不属于当前线程，lockKey: {}, threadId: {}", lockKey, threadId);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常，lockKey: {}, threadId: {}", lockKey, threadId, e);
        }
    }
}

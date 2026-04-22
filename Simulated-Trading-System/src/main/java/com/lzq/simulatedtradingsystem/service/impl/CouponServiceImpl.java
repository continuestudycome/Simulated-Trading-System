package com.lzq.simulatedtradingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.domain.Coupon;
import com.lzq.simulatedtradingsystem.domain.UserCoupon;
import com.lzq.simulatedtradingsystem.dto.CouponSeckillRequest;
import com.lzq.simulatedtradingsystem.mapper.CouponMapper;
import com.lzq.simulatedtradingsystem.mapper.UserCouponMapper;
import com.lzq.simulatedtradingsystem.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STREAM_KEY = "coupon:seckill:stream";
    private static final String GROUP_NAME = "seckill-group";
    private static final String CONSUMER_NAME = "consumer-1";

    @Override
    @Transactional
    public Result<String> seckillCoupon(CouponSeckillRequest request) {
        Long userId = request.getUserId();
        Long couponId = request.getCouponId();

        if (userId == null || couponId == null) {
            return Result.failure("参数不能为空");
        }

        String lockKey = "lock:coupon:" + couponId + ":user:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(5, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("用户 {} 获取优惠券 {} 的锁失败", userId, couponId);
                return Result.failure("系统繁忙，请稍后重试");
            }

            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null) {
                return Result.failure("优惠券不存在");
            }

            if (coupon.getRemainingQuantity() <= 0) {
                return Result.failure("优惠券已抢完");
            }

            long acquiredCount = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getCouponId, couponId));

            if (acquiredCount >= coupon.getPerLimit()) {
                return Result.failure("已达到每人限领数量：" + coupon.getPerLimit());
            }

            int updated = couponMapper.decreaseRemainingQuantity(couponId, 1);
            if (updated == 0) {
                return Result.failure("库存扣减失败");
            }

            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(couponId);
            userCoupon.setAcquireTime(LocalDateTime.now());
            userCouponMapper.insert(userCoupon);

            log.info("用户 {} 成功领取优惠券 {}", userId, couponId);
            return Result.success("领取成功");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断", e);
            return Result.failure("系统异常");
        } catch (Exception e) {
            log.error("优惠券秒杀异常", e);
            return Result.failure("系统异常：" + e.getMessage());
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Result<String> seckillCouponAsync(CouponSeckillRequest request) {
        Long userId = request.getUserId();
        Long couponId = request.getCouponId();

        if (userId == null || couponId == null) {
            return Result.failure("参数不能为空");
        }

        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            return Result.failure("优惠券不存在");
        }

        if (coupon.getRemainingQuantity() <= 0) {
            return Result.failure("优惠券已抢完");
        }

        try {
            Map<Object, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("couponId", couponId);

            RecordId recordId = redisTemplate.opsForStream().add(STREAM_KEY, message);

            log.info("秒杀消息已发送，recordId: {}, userId: {}, couponId: {}", 
                    recordId, userId, couponId);
            
            return Result.success("请求已接收，正在处理中");

        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.failure("系统异常");
        }
    }

    @Scheduled(fixedDelay = 1000)
    @SuppressWarnings("unchecked")
    public void consumeMessages() {
        try {
            initConsumerGroup();

            List<MapRecord<String, Object, Object>> records = 
                    redisTemplate.opsForStream().read(
                            Consumer.from(GROUP_NAME, CONSUMER_NAME),
                            StreamReadOptions.empty().count(10).block(java.time.Duration.ofSeconds(2)),
                            StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                    );

            if (records == null || records.isEmpty()) {
                return;
            }

            for (MapRecord<String, Object, Object> record : records) {
                try {
                    Map<Object, Object> value = record.getValue();
                    Long userId = Long.valueOf(value.get("userId").toString());
                    Long couponId = Long.valueOf(value.get("couponId").toString());

                    processSeckill(userId, couponId);

                    redisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, record.getId());
                    log.info("消息处理成功并ACK，recordId: {}", record.getId());

                } catch (Exception e) {
                    log.error("处理消息失败，recordId: {}", record.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("消费消息异常", e);
        }
    }

    private void initConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, GROUP_NAME);
        } catch (Exception e) {
            // 消费组已存在，忽略
        }
    }

    @Transactional
    public void processSeckill(Long userId, Long couponId) {
        log.info("开始处理秒杀，userId: {}, couponId: {}", userId, couponId);

        String lockKey = "lock:coupon:" + couponId + ":user:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(5, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取锁失败，userId: {}, couponId: {}", userId, couponId);
                return;
            }

            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null || coupon.getRemainingQuantity() <= 0) {
                return;
            }

            long acquiredCount = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getUserId, userId)
                    .eq(UserCoupon::getCouponId, couponId));

            if (acquiredCount >= coupon.getPerLimit()) {
                return;
            }

            int updated = couponMapper.decreaseRemainingQuantity(couponId, 1);
            if (updated == 0) {
                return;
            }

            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(couponId);
            userCoupon.setAcquireTime(LocalDateTime.now());
            userCouponMapper.insert(userCoupon);

            log.info("用户 {} 成功领取优惠券 {}（异步）", userId, couponId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("处理秒杀异常", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}

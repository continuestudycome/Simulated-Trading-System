package com.lzq.simulatedtradingsystem.controller;

import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.dto.CouponSeckillRequest;
import com.lzq.simulatedtradingsystem.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
@Slf4j
@Tag(name = "优惠券管理", description = "优惠券秒杀相关接口")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/seckill")
    @Operation(summary = "优惠券秒杀（同步）", description = "同步阻塞方式")
    public Result<String> seckillCoupon(@RequestBody CouponSeckillRequest request) {
        try {
            return couponService.seckillCoupon(request);
        } catch (RuntimeException e) {
            log.warn("业务异常: {}", e.getMessage());
            return Result.failure(e.getMessage());
        } catch (Exception e) {
            log.error("系统异常", e);
            return Result.failure("系统繁忙，请稍后重试");
        }
    }

    @PostMapping("/seckill/async")
    @Operation(summary = "优惠券秒杀（异步）", description = "基于Redis Stream异步处理")
    public Result<String> seckillCouponAsync(@RequestBody CouponSeckillRequest request) {
        try {
            return couponService.seckillCouponAsync(request);
        } catch (RuntimeException e) {
            log.warn("业务异常: {}", e.getMessage());
            return Result.failure(e.getMessage());
        } catch (Exception e) {
            log.error("系统异常", e);
            return Result.failure("系统繁忙，请稍后重试");
        }
    }
}

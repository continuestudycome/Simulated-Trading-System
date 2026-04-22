package com.lzq.simulatedtradingsystem.service;

import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.dto.CouponSeckillRequest;

public interface CouponService {
    
    Result<String> seckillCoupon(CouponSeckillRequest request);
    
    Result<String> seckillCouponAsync(CouponSeckillRequest request);
}

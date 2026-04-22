package com.lzq.simulatedtradingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzq.simulatedtradingsystem.domain.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    int decreaseRemainingQuantity(@Param("couponId") Long couponId, @Param("quantity") Integer quantity);
}

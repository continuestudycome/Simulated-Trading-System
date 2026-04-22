package com.lzq.simulatedtradingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponSeckillMessage {
    private Long userId;
    private Long couponId;
}

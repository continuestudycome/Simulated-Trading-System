package com.lzq.simulatedtradingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer totalQuantity;  // 总数
    private Integer remainingQuantity;  // 剩余数
    private Integer perLimit;   // 每人限购数
}

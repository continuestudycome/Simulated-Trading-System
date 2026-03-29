package com.lzq.simulatedtradingsystem.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order { // 订单
    private Long userId; // 用户ID
    private String stockCode; // 股票代码
    private Integer type; // 订单类型，1买 2卖
    private BigDecimal price; // 委托价格
    private Integer quantity; // 委托数量
    private Integer status; // 订单状态，0待处理 1已成交 2已撤单
    private LocalDateTime createTime; // 下单时间
}

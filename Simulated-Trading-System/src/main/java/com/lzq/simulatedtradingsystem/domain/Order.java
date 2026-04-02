package com.lzq.simulatedtradingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order { // 订单
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId; // 用户ID
    private String stockCode; // 股票代码
    private Integer type; // 订单类型，1 买 2 卖
    private BigDecimal price; // 委托价格
    private Integer quantity; // 委托数量
    private Integer status; // 订单状态，0 待处理 1 已成交 2 已撤单
    private LocalDateTime createTime; // 下单时间
}

package com.lzq.simulatedtradingsystem.domain;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("position")
public class Position { // 持仓
    @TableId(type = IdType.AUTO)
    private Long id;  // 持仓ID
    private Long userId;  // 用户ID
    private String stockCode;  // 股票代码
    private Integer quantity;  // 持仓数量（股数）
    private BigDecimal costPrice;  // 成本价（持仓均价）
}

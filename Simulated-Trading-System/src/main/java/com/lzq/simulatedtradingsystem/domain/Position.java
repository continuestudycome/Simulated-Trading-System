package com.lzq.simulatedtradingsystem.domain;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class Position { // 持仓
    private Long id;  // 持仓ID
    private Long userId;  // 用户ID
    private String stockCode;  // 股票代码
    private Integer quantity;  // 持仓数量（股数）
    private BigDecimal costPrice;  // 成本价（持仓均价）
}

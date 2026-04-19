package com.lzq.simulatedtradingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String stockCode;
    private Integer type;
    private BigDecimal price;
    private Integer quantity;
    private Integer status;
    private LocalDateTime createTime;
}

package com.lzq.simulatedtradingsystem.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("account")
public class Account {  // 账户
    @TableId(type = IdType.AUTO)
    private Long id; // 账户ID
    private Long userId; // 用户ID
    private BigDecimal balance; // 可用余额
    private BigDecimal frozen; // 冻结金额（下单时冻结的资金）
    private Integer version;    // 乐观锁版本号，用于并发控制
}

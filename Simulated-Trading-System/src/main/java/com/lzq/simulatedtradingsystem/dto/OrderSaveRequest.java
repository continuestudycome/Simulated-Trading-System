package com.lzq.simulatedtradingsystem.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderSaveRequest {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;  // Long，与数据库bigint对应

    @NotBlank(message = "股票代码不能为空")
    @Size(min = 6, max = 10, message = "股票代码长度为6-10位")
//    @Pattern(regexp = "^[0-9]{6}$|^[0-9]{6}\\.[A-Z]{2}$",
//            message = "股票代码格式不正确（A股6位数字，港股6位数字.SH/SZ）")
    private String stockCode;  // 与varchar(10)对应

    @NotNull(message = "订单类型不能为空")
    @Min(value = 1, message = "订单类型必须为1或2")
    @Max(value = 2, message = "订单类型必须为1或2")
    private Integer type;  // 与tinyint对应，1买 2卖

    @NotNull(message = "委托价格不能为空")
    @DecimalMin(value = "0.01", message = "委托价格最小为0.01元")
    @DecimalMax(value = "999999.99", message = "委托价格超出范围")
    @Digits(integer = 6, fraction = 2, message = "委托价格最多保留2位小数")
    private BigDecimal price;  // 与decimal(10,2)对应

    @NotNull(message = "委托数量不能为空")
    @Min(value = 100, message = "A股委托数量必须为100的整数倍且至少100股")
    @Max(value = 1000000, message = "委托数量不能超过1000000股")
    private Integer quantity;  // 与int对应

    @NotNull(message = "订单状态不能为空")
    @Min(value = 0, message = "订单状态必须为0、1或2")
    @Max(value = 2, message = "订单状态必须为0、1或2")
    private Integer status;  // 与tinyint对应，0待处理 1已成交 2已撤单

    @NotNull(message = "下单时间不能为空")
    @PastOrPresent(message = "下单时间不能晚于当前时间")
    private LocalDateTime createTime;  // 与datetime对应
}

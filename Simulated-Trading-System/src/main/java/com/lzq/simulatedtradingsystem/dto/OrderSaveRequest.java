package com.lzq.simulatedtradingsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "订单创建请求参数")
public class OrderSaveRequest {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotBlank(message = "股票代码不能为空")
    @Size(min = 6, max = 10, message = "股票代码长度为6-10位")
    @Schema(description = "股票代码", example = "600519")
    private String stockCode;

    @NotNull(message = "订单类型不能为空")
    @Min(value = 1, message = "订单类型必须为1或2")
    @Max(value = 2, message = "订单类型必须为1或2")
    @Schema(description = "订单类型：1-买入，2-卖出", example = "1")
    private Integer type;

    @NotNull(message = "委托价格不能为空")
    @DecimalMin(value = "0.01", message = "委托价格最小为0.01元")
    @DecimalMax(value = "999999.99", message = "委托价格超出范围")
    @Digits(integer = 6, fraction = 2, message = "委托价格最多保留2位小数")
    @Schema(description = "委托价格（元）", example = "1800.50")
    private BigDecimal price;

    @NotNull(message = "委托数量不能为空")
    @Min(value = 100, message = "A股委托数量必须为100的整数倍且至少100股")
    @Max(value = 1000000, message = "委托数量不能超过1000000股")
    @Schema(description = "委托数量（股）", example = "100")
    private Integer quantity;

    @NotNull(message = "订单状态不能为空")
    @Min(value = 0, message = "订单状态必须为0、1或2")
    @Max(value = 2, message = "订单状态必须为0、1或2")
    @Schema(description = "订单状态：0-待处理，1-已成交，2-已撤单", example = "0")
    private Integer status;

    @NotNull(message = "下单时间不能为空")
    @PastOrPresent(message = "下单时间不能晚于当前时间")
    @Schema(description = "下单时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createTime;
}

package com.lzq.simulatedtradingsystem.controller;

import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.dto.OrderSaveRequest;
import com.lzq.simulatedtradingsystem.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Slf4j
@Tag(name = "交易管理", description = "股票买入/卖出相关接口")
public class TradeController {
    private final TradeService tradeService;

    @PostMapping
    @Operation(summary = "创建交易订单", description = "执行股票买入或卖出操作")
    public Result<String> createOrder(@RequestBody OrderSaveRequest request) {
        try {
            return tradeService.createOrder(request);
        } catch (RuntimeException e) {
            log.warn("业务异常: {}", e.getMessage());
            return Result.failure(e.getMessage());
        } catch (Exception e) {
            log.error("系统异常", e);
            return Result.failure("系统繁忙，请稍后重试");
        }
    }


}

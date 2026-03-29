package com.lzq.simulatedtradingsystem.controller;

import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.dto.OrderSaveRequest;
import com.lzq.simulatedtradingsystem.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor    // 构造函数，自动注入依赖
@RequestMapping("/api/orders")
@Slf4j
public class TradeController {
    private final TradeService tradeService;

    // 股票买入/卖出
    @PostMapping
    public Result<?> createOrder(@RequestBody OrderSaveRequest request) {
        try {
            tradeService.createOrder(request);
            return Result.success();
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.failure(e.getMessage());
        }
    }


}

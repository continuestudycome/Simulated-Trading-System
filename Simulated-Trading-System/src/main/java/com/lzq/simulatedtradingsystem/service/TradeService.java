package com.lzq.simulatedtradingsystem.service;

import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.dto.OrderSaveRequest;

public interface TradeService {
    Result<String> createOrder(OrderSaveRequest orderSaveRequest);
}

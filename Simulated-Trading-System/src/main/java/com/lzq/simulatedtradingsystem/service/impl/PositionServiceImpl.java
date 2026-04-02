package com.lzq.simulatedtradingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.domain.Order;
import com.lzq.simulatedtradingsystem.domain.Position;
import com.lzq.simulatedtradingsystem.mapper.PositionMapper;
import com.lzq.simulatedtradingsystem.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionMapper positionMapper;
    // 查询持仓
}

package com.lzq.simulatedtradingsystem.service.impl;

import com.lzq.simulatedtradingsystem.domain.Order;
import com.lzq.simulatedtradingsystem.domain.Position;
import com.lzq.simulatedtradingsystem.service.PositionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PositionServiceImpl implements PositionService {

    // 查询持仓
    private void selectOne(Order order) {
        Position position = positionMapper.selectOne(new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, order.getUserId())
                .eq(Position::getStockCode, order.getStockCode()));
        BigDecimal totalCost = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        if (position == null) {
            position = new Position();
            position.setUserId(order.getUserId());
            position.setStockCode(order.getStockCode());
            position.setQuantity(order.getQuantity());
            position.setCostPrice(order.getPrice());
            positionMapper.insert(position);
        } else {
            // 移动平均法更新成本价
            BigDecimal oldTotalValue = position.getCostPrice().multiply(BigDecimal.valueOf(position.getQuantity()));
            BigDecimal newTotalValue = oldTotalValue.add(totalCost);
            int newQuantity = position.getQuantity() + order.getQuantity();
            BigDecimal newCostPrice = newTotalValue.divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);
            position.setQuantity(newQuantity);
            position.setCostPrice(newCostPrice);
            positionMapper.updateById(position);
        }

        // 解冻资金
        Account account = getAccountWithCache(order.getUserId());
        account.setFrozen(account.getFrozen().subtract(totalCost));
        updateAccount(account);

        // 更新订单状态
        order.setStatus(1);
        orderMapper.updateById(order);
    }
}

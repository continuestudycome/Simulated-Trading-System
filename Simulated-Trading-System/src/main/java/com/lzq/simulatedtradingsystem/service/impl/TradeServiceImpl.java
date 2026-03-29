package com.lzq.simulatedtradingsystem.service.impl;

import com.lzq.simulatedtradingsystem.dto.OrderSaveRequest;
import com.lzq.simulatedtradingsystem.mapper.OrderMapper;
import com.lzq.simulatedtradingsystem.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final OrderMapper orderMapper;
    @Override
    @Transactional // 开启事务
    public void createOrder(OrderSaveRequest order) {
//        orderSaveRequest.setCreateTime(LocalDateTime.now());
//        orderSaveRequest.setStatus(0);   // 0：待处理
//
//        // 1. 分布式锁
//        RLock lock = redissonClient.getLock("lock:user:" + userId);
//        try {
//            lock.lock(10, TimeUnit.SECONDS);
//
//            // 2. 获取账户（优先缓存）
//            Account account = getAccountWithCache(userId);
//            BigDecimal totalCost = price.multiply(BigDecimal.valueOf(quantity));
//
//            // 3. 风控校验
//            if (account.getBalance().compareTo(totalCost) < 0) {
//                return Result.error("资金不足");
//            }
//
//            // 4. 扣减资金，增加冻结
//            account.setBalance(account.getBalance().subtract(totalCost));
//            account.setFrozen(account.getFrozen().add(totalCost));
//            updateAccount(account); // 更新数据库+缓存
//
//            // 5. 创建订单（待成交）
//            Order order = new Order();
//            order.setUserId(userId);
//            order.setStockCode(stockCode);
//            order.setType(1);
//            order.setPrice(price);
//            order.setQuantity(quantity);
//            order.setStatus(0);
//            order.setCreateTime(new Date());
//            orderMapper.insert(order);
//
//            // 6. 模拟成交（实际应异步，此处简化）
//            executeTrade(order);
//
//            return Result.success("买入成功");
//        } catch (Exception e) {
//            log.error("买入异常", e);
//            return Result.error("系统异常");
//        } finally {
//            if (lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
//        }
    }

    // 执行交易
//    private void executeTrade(Order order) {
//        Position position = positionMapper.selectOne(new LambdaQueryWrapper<Position>()
//                .eq(Position::getUserId, order.getUserId())
//                .eq(Position::getStockCode, order.getStockCode()));
//        BigDecimal totalCost = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
//
//        if (position == null) {
//            position = new Position();
//            position.setUserId(order.getUserId());
//            position.setStockCode(order.getStockCode());
//            position.setQuantity(order.getQuantity());
//            position.setCostPrice(order.getPrice());
//            positionMapper.insert(position);
//        } else {
//            // 移动平均法更新成本价
//            BigDecimal oldTotalValue = position.getCostPrice().multiply(BigDecimal.valueOf(position.getQuantity()));
//            BigDecimal newTotalValue = oldTotalValue.add(totalCost);
//            int newQuantity = position.getQuantity() + order.getQuantity();
//            BigDecimal newCostPrice = newTotalValue.divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);
//            position.setQuantity(newQuantity);
//            position.setCostPrice(newCostPrice);
//            positionMapper.updateById(position);
//        }
//
//        // 解冻资金
//        Account account = getAccountWithCache(order.getUserId());
//        account.setFrozen(account.getFrozen().subtract(totalCost));
//        updateAccount(account);
//
//        // 更新订单状态
//        order.setStatus(1);
//        orderMapper.updateById(order);
//    }
}

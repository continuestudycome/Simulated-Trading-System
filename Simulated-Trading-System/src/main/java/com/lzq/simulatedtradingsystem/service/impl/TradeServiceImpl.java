package com.lzq.simulatedtradingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.domain.Order;
import com.lzq.simulatedtradingsystem.domain.Position;
import com.lzq.simulatedtradingsystem.dto.OrderSaveRequest;
import com.lzq.simulatedtradingsystem.mapper.OrderMapper;
import com.lzq.simulatedtradingsystem.mapper.PositionMapper;
import com.lzq.simulatedtradingsystem.service.AccountService;
import com.lzq.simulatedtradingsystem.service.TradeService;
import com.lzq.simulatedtradingsystem.utils.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeServiceImpl implements TradeService {

    private final OrderMapper orderMapper;
    private final PositionMapper positionMapper;
    private final RedissonClient redissonClient;
    private final AccountService accountService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional
    public Result<String> createOrder(OrderSaveRequest orderRequest) {
        // 获取参数
        Long userId = orderRequest.getUserId();
        String stockCode = orderRequest.getStockCode();
        BigDecimal price = orderRequest.getPrice();
        Integer quantity = orderRequest.getQuantity();
        Integer type = orderRequest.getType(); // 1:买入, 2:卖出

        // 参数校验
        if (userId == null || stockCode == null || price == null || quantity == null || type == null) {
            return Result.failure("参数不能为空");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0 || quantity <= 0) {
            return Result.failure("价格或数量必须大于0");
        }
        if (type != 1 && type != 2) {
            return Result.failure("订单类型错误，1为买入，2为卖出");
        }

        // 检查用户账户是否存在
        Account account = accountService.getAccountWithCache(userId);
        if (account == null) {
            log.warn("用户 {} 不存在，拒绝交易请求", userId);
            return Result.failure("用户账户不存在，请先注册");
        }

        // 创建订单对象
        Order order = new Order();
        order.setId(snowflakeIdGenerator.nextId());
        order.setUserId(userId);
        order.setStockCode(stockCode);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setType(type);
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(0); // 0：待处理

        // 分布式锁
        RLock lock = redissonClient.getLock("lock:user:" + userId);
        try {
            lock.lock(10, TimeUnit.SECONDS);

            if (type == 1) {
                // 买入逻辑
                Result<String> buyResult = handleBuyOrder(order);
                if (!buyResult.isSuccess()) {
                    return buyResult;
                }
            } else if (type == 2) {
                // 卖出逻辑
                Result<String> sellResult = handleSellOrder(order);
                if (!sellResult.isSuccess()) {
                    return sellResult;
                }
            }

            // 保存订单
            orderMapper.insert(order);

            // 执行交易
            executeTrade(order);

            return Result.success(type == 1 ? "买入成功" : "卖出成功");

        } catch (Exception e) {
            log.error("订单处理异常", e);
            return Result.failure("系统异常：" + e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 处理买入订单的资金校验和冻结
     */
    private Result<String> handleBuyOrder(Order order) {
        Account account = accountService.getAccountWithCache(order.getUserId());
        
        if (account == null) {
            return Result.failure("用户账户不存在，请先注册");
        }
        
        BigDecimal totalCost = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        // 风控校验：资金是否充足
        if (account.getBalance().compareTo(totalCost) < 0) {
            return Result.failure("资金不足，当前余额：" + account.getBalance() + "元");
        }

        // 扣减可用资金，增加冻结资金
        account.setBalance(account.getBalance().subtract(totalCost));
        account.setFrozen(account.getFrozen().add(totalCost));
        accountService.updateAccount(account);
        
        return Result.success();
    }

    /**
     * 处理卖出订单的资金校验和冻结
     */
    private Result<String> handleSellOrder(Order order) {
        // 查询持仓
        Position position = positionMapper.selectOne(new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, order.getUserId())
                .eq(Position::getStockCode, order.getStockCode()));

        // 风控校验：是否持有该股票
        if (position == null) {
            return Result.failure("未持有该股票");
        }

        // 风控校验：持仓数量是否足够
        if (position.getQuantity() < order.getQuantity()) {
            return Result.failure("持仓数量不足，当前持有：" + position.getQuantity() + "股");
        }

        BigDecimal totalAmount = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        // 卖出时，资金不冻结，直接增加可用资金
        Account account = accountService.getAccountWithCache(order.getUserId());
        
        if (account == null) {
            return Result.failure("用户账户不存在");
        }
        
        account.setBalance(account.getBalance().add(totalAmount));
        accountService.updateAccount(account);
        
        return Result.success();
    }

    /**
     * 执行交易（更新持仓和订单状态）
     */
    private void executeTrade(Order order) {
        if (order.getType() == 1) {
            // 买入：增加持仓
            executeBuyTrade(order);
        } else if (order.getType() == 2) {
            // 卖出：减少持仓
            executeSellTrade(order);
        }

        // 更新订单状态为已成交
        order.setStatus(1);
        orderMapper.updateById(order);
    }

    /**
     * 执行买入交易：增加持仓，解冻资金
     */
    private void executeBuyTrade(Order order) {
        Position position = positionMapper.selectOne(new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, order.getUserId())
                .eq(Position::getStockCode, order.getStockCode()));

        BigDecimal totalCost = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        if (position == null) {
            // 新增持仓
            position = new Position();
            position.setUserId(order.getUserId());
            position.setStockCode(order.getStockCode());
            position.setQuantity(order.getQuantity());
            position.setCostPrice(order.getPrice());
            positionMapper.insert(position);
        } else {
            // 移动平均法更新成本价
            BigDecimal oldTotalValue = position.getCostPrice()
                    .multiply(BigDecimal.valueOf(position.getQuantity()));
            BigDecimal newTotalValue = oldTotalValue.add(totalCost);
            int newQuantity = position.getQuantity() + order.getQuantity();
            BigDecimal newCostPrice = newTotalValue
                    .divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);

            position.setQuantity(newQuantity);
            position.setCostPrice(newCostPrice);
            positionMapper.updateById(position);
        }

        // 解冻资金
        Account account = accountService.getAccountWithCache(order.getUserId());
        if (account != null) {
            account.setFrozen(account.getFrozen().subtract(totalCost));
            accountService.updateAccount(account);
        }
    }

    /**
     * 执行卖出交易：减少持仓，资金已在 handleSellOrder 中增加
     */
    private void executeSellTrade(Order order) {
        Position position = positionMapper.selectOne(new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, order.getUserId())
                .eq(Position::getStockCode, order.getStockCode()));

        if (position == null) {
            log.error("执行卖出交易时持仓不存在，orderId: {}", order.getId());
            return;
        }

        int newQuantity = position.getQuantity() - order.getQuantity();

        if (newQuantity == 0) {
            // 全部卖出，删除持仓记录
            positionMapper.deleteById(position.getId());
        } else {
            // 部分卖出，更新持仓数量（成本价不变）
            position.setQuantity(newQuantity);
            positionMapper.updateById(position);
        }

        // 计算并保存盈亏
        calculateAndSaveProfitLoss(order, position);
    }

    /**
     * 计算并保存卖出盈亏（可选功能）
     */
    private void calculateAndSaveProfitLoss(Order order, Position position) {
        BigDecimal sellAmount = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        BigDecimal costAmount = position.getCostPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        BigDecimal profitLoss = sellAmount.subtract(costAmount);

        // 可以将盈亏保存到订单表的扩展字段
        log.info("卖出盈亏：{}，卖出金额：{}，成本金额：{}", profitLoss, sellAmount, costAmount);

        // 如果Order表有profit_loss字段，可以设置
        // order.setProfitLoss(profitLoss);
        // orderMapper.updateById(order);
    }
}

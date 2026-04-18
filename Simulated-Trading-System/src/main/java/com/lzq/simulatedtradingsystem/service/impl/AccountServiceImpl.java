package com.lzq.simulatedtradingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.mapper.AccountMapper;
import com.lzq.simulatedtradingsystem.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountMapper accountMapper;
    private final StringRedisTemplate stringRedisTemplate;  // Redis 缓存
    private static final ObjectMapper mapper = new ObjectMapper();  // JSON 序列化

    private static final String ACCOUNT_CACHE_KEY = "sty:account:"; // Redis 缓存 key

    /**
     * 根据用户 ID 查询账户信息
     */
    @Override
    public List<Account> findByUserId(Long id) {
        return accountMapper.findByUserId(id);
    }

    /**
     * 根据用户 ID 在 Redis 查询账户信息，没有就从数据库查询并使用 Redis 缓存
     */
    @Override
    public Account getAccountWithCache(Long userId) {
        String key = ACCOUNT_CACHE_KEY + userId;
        String val = stringRedisTemplate.opsForValue().get(key);

        Account account = null;
        if (val != null && !val.isEmpty()) {
            try {
                account = mapper.readValue(val, Account.class);
            } catch (Exception e) {
                // JSON 反序列化失败，忽略缓存，从数据库查询
            }
        }

        if (account == null) {
            // 缓存未命中，从数据库查询账户信息
            // accountMapper.selectOne(): MyBatis-Plus 方法，查询单条记录
            // new LambdaQueryWrapper<Account>(): 创建 Lambda 查询条件构造器（类型安全）
            // .eq(Account::getUserId, userId): 添加等值查询条件
            //   - Account::getUserId: 方法引用，指向 Account 类的 getUserId() 方法
            //   - userId: 查询参数值
            // 生成的 SQL: SELECT * FROM account WHERE user_id = ?
            account = accountMapper.selectOne(new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId));
            if (account != null) {
                try {
                    String json = mapper.writeValueAsString(account);
                    stringRedisTemplate.opsForValue().set(key, json, 10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    // JSON 序列化失败，忽略缓存
                }
            } else {
                account = new Account();
                account.setUserId(userId);
                account.setBalance(new BigDecimal("100000.00"));
                account.setFrozen(new BigDecimal("0.00"));
                account.setVersion(0);
                accountMapper.insert(account);
                try {
                    String json = mapper.writeValueAsString(account);
                    stringRedisTemplate.opsForValue().set(key, json, 10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    // JSON 序列化失败，忽略缓存
                }
            }
        }
        return account;
    }

    @Override
    public void updateAccount(Account account) {
        accountMapper.updateById(account);
        String key = ACCOUNT_CACHE_KEY + account.getUserId();
        try {
            String json = mapper.writeValueAsString(account);
            stringRedisTemplate.opsForValue().set(key, json, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            // JSON 序列化失败，忽略缓存更新
        }
    }
}

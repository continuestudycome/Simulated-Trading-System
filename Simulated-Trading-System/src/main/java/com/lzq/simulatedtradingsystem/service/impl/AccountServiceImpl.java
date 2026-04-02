package com.lzq.simulatedtradingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.mapper.AccountMapper;
import com.lzq.simulatedtradingsystem.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountMapper accountMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACCOUNT_CACHE_KEY = "account:";

    @Override
    public List<Account> findByUserId(Long id) {
        return accountMapper.findByUserId(id);
    }

    @Override
    public Account getAccountWithCache(Long userId) {
        String key = ACCOUNT_CACHE_KEY + userId;        // 构造 Redis key，如 account:1001。
        // 从 Redis 中获取 value
        Account account = (Account) redisTemplate.opsForValue().get(key);
        if (account == null) {  // 如果缓存不存在（第一次访问或缓存过期），则从数据库查询。
            account = accountMapper.selectOne(new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId));
            if (account != null) {
                redisTemplate.opsForValue().set(key, account);
            }
        }
        return account;
    }

    @Override
    public void updateAccount(Account account) {
        // 更新数据库
        accountMapper.updateById(account);
        // 更新缓存
        String key = ACCOUNT_CACHE_KEY + account.getUserId();
        redisTemplate.opsForValue().set(key, account);
    }
}

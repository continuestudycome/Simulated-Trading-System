package com.lzq.simulatedtradingsystem.service.impl;

import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.mapper.AccountMapper;
import com.lzq.simulatedtradingsystem.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountMapper accountMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Account> findByUserId(Long id) {
        return accountMapper.findByUserId(id);
    }

    private Account getAccountWithCache(Long userId) {
        String key = ACCOUNT_CACHE_KEY + userId;
        Account account = (Account) redisTemplate.opsForValue().get(key);
        if (account == null) {
            account = accountMapper.selectOne(new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId));
            if (account != null) {
                redisTemplate.opsForValue().set(key, account);
            }
        }
        return account;
    }

    private void updateAccount(Account account) {
        // 更新数据库
        accountMapper.updateById(account);
        // 更新缓存
        String key = ACCOUNT_CACHE_KEY + account.getUserId();
        redisTemplate.opsForValue().set(key, account);
    }
}

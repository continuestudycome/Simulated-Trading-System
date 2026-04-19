package com.lzq.simulatedtradingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.mapper.AccountMapper;
import com.lzq.simulatedtradingsystem.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountMapper accountMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String ACCOUNT_CACHE_KEY = "sty:account:";
    private static final String EMPTY_ACCOUNT_MARKER = "empty";
    private static final long NORMAL_CACHE_EXPIRE = 10;
    private static final long EMPTY_CACHE_EXPIRE = 1;

    private static final String FIELD_ID = "id";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_BALANCE = "balance";
    private static final String FIELD_FROZEN = "frozen";
    private static final String FIELD_VERSION = "version";

    @Override
    public List<Account> findByUserId(Long id) {
        return accountMapper.findByUserId(id);
    }

    @Override
    public Account getAccountWithCache(Long userId) {
        String key = ACCOUNT_CACHE_KEY + userId;

        Boolean exists = stringRedisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            String marker = stringRedisTemplate.opsForHash().get(key, "marker") != null 
                    ? stringRedisTemplate.opsForHash().get(key, "marker").toString() 
                    : null;
            
            if (EMPTY_ACCOUNT_MARKER.equals(marker)) {
                log.warn("用户 {} 不存在（命中空对象缓存）", userId);
                return null;
            }

            try {
                Account account = hashToAccount(key);
                if (account != null) {
                    log.debug("用户 {} 账户信息从缓存加载", userId);
                    return account;
                }
            } catch (Exception e) {
                log.error("用户 {} 账户缓存读取失败，将从数据库查询", userId, e);
                stringRedisTemplate.delete(key);
            }
        }

        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId)
        );

        if (account != null) {
            cacheAccount(key, account);
            log.debug("用户 {} 账户信息已缓存（{} 分钟）", userId, NORMAL_CACHE_EXPIRE);
        } else {
            cacheEmptyAccount(key);
            log.warn("用户 {} 不存在，已缓存空对象标记（{} 分钟）", userId, EMPTY_CACHE_EXPIRE);
        }

        return account;
    }

    @Override
    public void updateAccount(Account account) {
        accountMapper.updateById(account);
        
        String key = ACCOUNT_CACHE_KEY + account.getUserId();
        try {
            Map<String, String> map = new HashMap<>();
            map.put(FIELD_ID, account.getId().toString());
            map.put(FIELD_USER_ID, account.getUserId().toString());
            map.put(FIELD_BALANCE, account.getBalance().toString());
            map.put(FIELD_FROZEN, account.getFrozen().toString());
            map.put(FIELD_VERSION, account.getVersion().toString());
            
            stringRedisTemplate.opsForHash().putAll(key, map);
            stringRedisTemplate.expire(key, NORMAL_CACHE_EXPIRE, TimeUnit.MINUTES);
            
            log.debug("用户 {} 账户信息更新并刷新缓存", account.getUserId());
        } catch (Exception e) {
            log.error("用户 {} 账户缓存更新失败", account.getUserId(), e);
        }
    }

    private Account hashToAccount(String key) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        
        if (entries == null || entries.isEmpty()) {
            return null;
        }

        Account account = new Account();
        
        if (entries.containsKey(FIELD_ID)) {
            account.setId(Long.parseLong(entries.get(FIELD_ID).toString()));
        }
        if (entries.containsKey(FIELD_USER_ID)) {
            account.setUserId(Long.parseLong(entries.get(FIELD_USER_ID).toString()));
        }
        if (entries.containsKey(FIELD_BALANCE)) {
            account.setBalance(new BigDecimal(entries.get(FIELD_BALANCE).toString()));
        }
        if (entries.containsKey(FIELD_FROZEN)) {
            account.setFrozen(new BigDecimal(entries.get(FIELD_FROZEN).toString()));
        }
        if (entries.containsKey(FIELD_VERSION)) {
            account.setVersion(Integer.parseInt(entries.get(FIELD_VERSION).toString()));
        }
        
        return account;
    }

    private void cacheAccount(String key, Account account) {
        Map<String, String> map = new HashMap<>();
        map.put(FIELD_ID, account.getId().toString());
        map.put(FIELD_USER_ID, account.getUserId().toString());
        map.put(FIELD_BALANCE, account.getBalance().toString());
        map.put(FIELD_FROZEN, account.getFrozen().toString());
        map.put(FIELD_VERSION, account.getVersion().toString());
        
        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, NORMAL_CACHE_EXPIRE, TimeUnit.MINUTES);
    }

    private void cacheEmptyAccount(String key) {
        stringRedisTemplate.opsForHash().put(key, "marker", EMPTY_ACCOUNT_MARKER);
        stringRedisTemplate.expire(key, EMPTY_CACHE_EXPIRE, TimeUnit.MINUTES);
    }
}

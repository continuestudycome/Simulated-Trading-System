package com.lzq.simulatedtradingsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.mapper.AccountMapper;
import com.lzq.simulatedtradingsystem.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountMapper accountMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String ACCOUNT_CACHE_KEY = "sty:account:";
    // 空对象缓存标记：用于标识数据库中不存在的用户
    private static final String EMPTY_ACCOUNT_MARKER = "{}";
    // 正常账户缓存过期时间：10 分钟
    private static final long NORMAL_CACHE_EXPIRE = 10;
    // 空对象缓存过期时间：1 分钟（防止缓存穿透，同时避免长时间缓存导致新用户无法注册）
    private static final long EMPTY_CACHE_EXPIRE = 1;

    /**
     * 根据用户 ID 查询账户信息
     */
    @Override
    public List<Account> findByUserId(Long id) {
        return accountMapper.findByUserId(id);
    }

    /**
     * 根据用户 ID 在 Redis 查询账户信息，没有就从数据库查询并使用 Redis 缓存
     * 采用缓存空对象策略解决缓存穿透问题
     */
    @Override
    public Account getAccountWithCache(Long userId) {
        String key = ACCOUNT_CACHE_KEY + userId;
        String val = stringRedisTemplate.opsForValue().get(key);

        // 第一步：检查缓存是否存在
        if (val != null && !val.isEmpty()) {
            // 第二步：判断是否为空对象标记（缓存穿透防护）
            if (EMPTY_ACCOUNT_MARKER.equals(val)) {
                log.warn("用户 {} 不存在（命中空对象缓存）", userId);
                return null;
            }

            // 第三步：尝试反序列化正常账户数据
            try {
                Account account = mapper.readValue(val, Account.class);
                log.debug("用户 {} 账户信息从缓存加载", userId);
                return account;
            } catch (Exception e) {
                log.error("用户 {} 账户缓存反序列化失败，将从数据库查询", userId, e);
                // 反序列化失败，删除脏缓存，继续从数据库查询
                stringRedisTemplate.delete(key);
            }
        }

        // 第四步：缓存未命中，查询数据库
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId)
        );

        if (account != null) {
            // 第五步：数据库中存在，缓存正常数据
            try {
                String json = mapper.writeValueAsString(account);
                stringRedisTemplate.opsForValue().set(key, json, NORMAL_CACHE_EXPIRE, TimeUnit.MINUTES);
                log.debug("用户 {} 账户信息已缓存（{} 分钟）", userId, NORMAL_CACHE_EXPIRE);
            } catch (Exception e) {
                log.error("用户 {} 账户缓存序列化失败", userId, e);
            }
        } else {
            // 第六步：数据库中不存在，缓存空对象标记（防止缓存穿透）
            stringRedisTemplate.opsForValue().set(key, EMPTY_ACCOUNT_MARKER, EMPTY_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.warn("用户 {} 不存在，已缓存空对象标记（{} 分钟）", userId, EMPTY_CACHE_EXPIRE);
        }

        return account;
    }

    @Override
    public void updateAccount(Account account) {
        accountMapper.updateById(account);
        String key = ACCOUNT_CACHE_KEY + account.getUserId();
        try {
            String json = mapper.writeValueAsString(account);
            stringRedisTemplate.opsForValue().set(key, json, NORMAL_CACHE_EXPIRE, TimeUnit.MINUTES);
            log.debug("用户 {} 账户信息更新并刷新缓存", account.getUserId());
        } catch (Exception e) {
            log.error("用户 {} 账户缓存更新失败", account.getUserId(), e);
        }
    }
}

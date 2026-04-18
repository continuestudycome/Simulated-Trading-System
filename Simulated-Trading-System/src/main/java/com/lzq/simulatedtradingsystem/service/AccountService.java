package com.lzq.simulatedtradingsystem.service;

import com.lzq.simulatedtradingsystem.domain.Account;

import java.util.List;

public interface AccountService {

    // 通过用户id查询账户信息
    List<Account> findByUserId(Long id);

    Account getAccountWithCache(Long userId);

    void updateAccount(Account account);

}

package com.lzq.simulatedtradingsystem.service;

import com.lzq.simulatedtradingsystem.domain.Account;

import java.util.List;

public interface AccountService {

    List<Account> findByUserId(Long id);

}

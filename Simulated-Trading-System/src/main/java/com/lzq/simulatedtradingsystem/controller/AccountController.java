package com.lzq.simulatedtradingsystem.controller;

import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@Slf4j
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/{id}")
    public Result<List<Account>> getAccountByUserId(@PathVariable Long id) {
        return Result.success(accountService.findByUserId(id));
    }
}

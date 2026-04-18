package com.lzq.simulatedtradingsystem.controller;

import com.lzq.simulatedtradingsystem.common.Result;
import com.lzq.simulatedtradingsystem.domain.Account;
import com.lzq.simulatedtradingsystem.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "账户管理", description = "用户账户信息查询接口")
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/{id}")
    @Operation(summary = "查询用户账户", description = "根据用户ID查询账户信息（余额、冻结资金等）")
    public Result<List<Account>> getAccountByUserId(
            @Parameter(description = "用户ID", example = "1")
            @PathVariable Long id) {
        return Result.success(accountService.findByUserId(id));
    }
}

package com.lzq.simulatedtradingsystem.mapper;

import com.lzq.simulatedtradingsystem.domain.Account;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper {

    List<Account> findByUserId(Long id);
}

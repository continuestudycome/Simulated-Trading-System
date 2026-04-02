package com.lzq.simulatedtradingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzq.simulatedtradingsystem.domain.Account;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    List<Account> findByUserId(Long id);
}

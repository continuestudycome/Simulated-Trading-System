package com.lzq.simulatedtradingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzq.simulatedtradingsystem.domain.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}

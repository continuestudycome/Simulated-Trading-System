package com.lzq.simulatedtradingsystem.mapper;

import com.lzq.simulatedtradingsystem.dto.OrderSaveRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    void insert(OrderSaveRequest orderSaveRequest);

}

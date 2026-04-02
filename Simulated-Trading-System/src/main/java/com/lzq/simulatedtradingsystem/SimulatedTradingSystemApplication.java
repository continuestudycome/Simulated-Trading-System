package com.lzq.simulatedtradingsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lzq.simulatedtradingsystem.mapper")
public class SimulatedTradingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatedTradingSystemApplication.class, args);
    }

}

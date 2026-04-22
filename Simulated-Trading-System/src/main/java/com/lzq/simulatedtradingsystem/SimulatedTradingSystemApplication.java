package com.lzq.simulatedtradingsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.lzq.simulatedtradingsystem.mapper")
@EnableScheduling
public class SimulatedTradingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulatedTradingSystemApplication.class, args);
    }
}

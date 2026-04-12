package com.lzq.simulatedtradingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 只允许你的前端地址（替换掉原来的允许所有来源）
        config.addAllowedOrigin("http://localhost:5173");
        // 如果你的后端也部署在服务器上，可能需要加上服务器IP，例如：
        // config.addAllowedOrigin("http://127.0.0.1:5173");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有请求方法
        config.addAllowedMethod("*");

        // 允许携带凭证（cookies）
        config.setAllowCredentials(true);

        // 预检请求的有效期（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}

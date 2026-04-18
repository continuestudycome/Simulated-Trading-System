package com.lzq.simulatedtradingsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("模拟交易系统 API 文档")
                        .version("1.0.0")
                        .description("基于 Spring Boot 4.0 + MyBatis Plus + Redis + Redisson的模拟股票交易系统")
                        .contact(new Contact()
                                .name("开发团队:爱吃水果的L")
                                .email("liaozhiqiang2004@outlook.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

package com.lzq.simulatedtradingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 创建 RedisTemplate 对象，
     * 并设置序列化器（因为默认使用jdk的序列化器，会把对象转换成字节存入，可读性差）
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        // 键使用 String 序列化器
//        StringRedisSerializer stringSerializer = new StringRedisSerializer();
//        template.setKeySerializer(stringSerializer);
//        template.setHashKeySerializer(stringSerializer);
//
//        // 使用新的 RedisSerializer.json() 方法（Spring Data Redis 4.0+）
//        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();
//
//        template.setValueSerializer(jsonSerializer);
//        template.setHashValueSerializer(jsonSerializer);
//
//        template.afterPropertiesSet();
//        return template;

            // 创建Template
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
            // 设置连接工厂
            redisTemplate.setConnectionFactory(connectionFactory);

            // key和hashKey采用string序列化
            redisTemplate.setKeySerializer(RedisSerializer.string());
            redisTemplate.setHashKeySerializer(RedisSerializer.string());

            // value和hashValue采用JSON序列化（使用 Spring Data Redis 4.0+ 推荐的方式）
            RedisSerializer<Object> jsonSerializer = RedisSerializer.json();
            redisTemplate.setValueSerializer(jsonSerializer);
            redisTemplate.setHashValueSerializer(jsonSerializer);

            return redisTemplate;
    }
}


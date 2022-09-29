package com.clarity.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 操作 Redis 配置类
 *
 * @author: clarity
 * @date: 2022年09月28日 19:16
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    // redis 端口
    private String port;
    // redis 主机名
    private String host;

    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置
        Config config = new Config();
        // 当个 redis 的配置创建
        // 设置 redis 3 库专门选择一个干净的库存放分布式信息
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}

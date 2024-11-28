package org.oastore.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 配置 Redis 服务器地址和端口等
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 可以根据需要添加更多配置，如密码、连接池设置等
        // 例如：config.useSingleServer().setPassword("yourpassword").setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }
}

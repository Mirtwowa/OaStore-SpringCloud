package org.example.oastoreaop;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class RedisTest {
        public static JSON speedOfRedis() {
            // Redis 配置
            String redisHost = "localhost";
            int redisPort = 6379;

            // 连接 Redis
            try (Jedis redisClient = new Jedis(redisHost, redisPort)) {
                // 使用 SCAN 查询所有 user 表相关的键
                String pattern = "user:*"; // 匹配所有 user 的键
                Set<String> userKeys = redisClient.keys(pattern); // 可替换为更高效的 SCAN 方法

                if (userKeys.isEmpty()) {
                    System.out.println("未找到任何 user 数据！");
                    return null;
                }
                return JSON.parseArray(JSON.toJSONString(userKeys));
            }
        }

    public static void main(String[] args) {
           JSON json = speedOfRedis();
        System.out.println(json);
    }
}



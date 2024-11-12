package org.example.oastoreaop.service;


import org.example.oastoreaop.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class RabbitMqService {
    @Autowired
    private RabbitMqConfig rabbitMqConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String REDIS_KEY_PREFIX = "UserMessage:";
    Integer index= 0;
    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME,concurrency = "3-10")
    @Transactional
    public void handleUserMessage(String message) {
        ValueOperations<String,String> operations = redisTemplate.opsForValue();
        // 2. 插入到Redis
        operations.set(REDIS_KEY_PREFIX + index++ , message,2, TimeUnit.MINUTES);
    }
}

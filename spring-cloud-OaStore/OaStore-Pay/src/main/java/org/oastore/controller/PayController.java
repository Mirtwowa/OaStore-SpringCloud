package org.oastore.controller;

import org.example.common.vo.Product;
import org.example.common.vo.User;
import org.oastore.mapper.ProductMapper;
import org.oastore.mapper.UserMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@RestController
public class PayController {
    private static final Logger logger = LoggerFactory.getLogger(PayController.class);
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    private final ExecutorService executorService;

    public PayController() {
        this.executorService = Executors.newCachedThreadPool();;
    }

    public static String findTable(Integer userId){
        String tableName;
        if (userId / 10000000 == 1) {
            tableName = "general_managers";
        } else if (userId / 10000000 == 2) {
            tableName = "department_heads";
        } else if (userId / 10000000 == 3) {
            tableName = "project_managers";
        } else if (userId / 10000000 == 4) {
            tableName = "employees";
        } else {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
        return tableName;
    }

    @GetMapping("/getAllProducts")
    public List<Product> getAllProducts() {
        return productMapper.findAll();
    }

    @PostMapping("/getUser")
    public User consume(Integer userId) {
        String tableName = findTable(userId);
        return userMapper.getUser(tableName,userId);
    }
    @Transactional
    @PostMapping("/consume")
    public User consume(@RequestParam Integer userId, @RequestParam Integer productId) {
        // 使用用户ID生成分布式锁的唯一标识
        String lockKey = "user:lock:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待5秒，持有时间10秒
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                // 异步执行余额更新操作
                CompletableFuture<User> future = CompletableFuture.supplyAsync(() -> {
                    // 获取商品信息
                    Product amount = productMapper.findById(productId);
                    if (amount == null) {
                        logger.error("商品不存在！");
                        throw new RuntimeException("商品不存在！");
                    }

                    if (Objects.equals(amount.getCategory(), "0")) {
                        logger.info("商品售罄！");
                        throw new RuntimeException("商品已售罄！");
                    }

                    // 确定分表
                    String tableName = findTable(userId);

                    // 更新用户余额，调用异步方法
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", userId);
                    map.put("amount", amount.getPrice());
                    map.put("tableName", tableName);

                    // 更新用户余额
                    Integer updateResult = userMapper.updateUserBalance(map);

                    // 等待操作结果
                    // 获取操作返回的结果
                    if (updateResult == 0) {
                        logger.error("余额更新失败！");
                        throw new RuntimeException("余额更新失败！");
                    }

                    logger.info("支付成功！");

                    // 返回更新后的用户信息
                    User updatedUser = userMapper.getUser(tableName, userId);
                    if (updatedUser == null) {
                        logger.error("用户信息获取失败！");
                        throw new RuntimeException("用户信息获取失败！");
                    }

                    return updatedUser;
                });

                // 同步获取任务执行结果
                return future.get();  // 阻塞并获取结果
            } else {
                logger.error("获取用户锁失败，操作超时");
                throw new RuntimeException("获取用户锁失败，操作超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("线程中断异常: {}", e.getMessage());
            throw new RuntimeException("线程中断异常", e);
        } catch (ExecutionException e) {
            logger.error("任务执行异常: {}", e.getCause().getMessage());
            throw new RuntimeException("任务执行异常", e.getCause());
        } finally {
            // 确保释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            logger.info("当前线程资源释放");
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}


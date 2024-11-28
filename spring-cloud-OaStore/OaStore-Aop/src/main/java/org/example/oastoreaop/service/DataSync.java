package org.example.oastoreaop.service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class DataSync {
    // MySQL 配置
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/java";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "123456";

    // Redis 配置
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    // 线程池配置
    private static final int THREAD_POOL_SIZE = 10; // 最大并发数
    private static final ExecutorService executorService = new ThreadPoolExecutor(
            THREAD_POOL_SIZE,
            THREAD_POOL_SIZE,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy() // 提供合理的失败处理策略
    );

    // 最大重试次数
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000; // 5秒延迟重试

    // Redis 键的过期时间，单位秒
    private static final long REDIS_KEY_EXPIRATION_TIME = 3600L; // 设置为1小时

    public static void main(String[] args) {
        try (Connection mysqlConnection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
             Jedis redisClient = new Jedis(REDIS_HOST, REDIS_PORT)) {

            // 获取所有表名
            List<String> tableNames = getTableNames(mysqlConnection);
            System.out.println("同步表：" + tableNames);

            // 分页同步数据
            for (String tableName : tableNames) {
                processTableData(mysqlConnection, redisClient, tableName);
            }

            System.out.println("数据同步完成！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    // 获取所有表名
    private static List<String> getTableNames(Connection mysqlConnection) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = mysqlConnection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        while (tables.next()) {
            tableNames.add(tables.getString("TABLE_NAME"));
        }
        return tableNames;
    }

    // 处理每个表的数据
    private static void processTableData(Connection mysqlConnection, Jedis redisClient, String tableName) {
        executorService.submit(() -> {
            try {
                int offset = 0;
                int batchSize = 1000; // 每次处理1000条数据
                boolean hasMoreData = true;

                // 获取增量同步的时间戳字段（如更新时间）
                String lastSyncTime = getLastSyncTime(redisClient, tableName);

                while (hasMoreData) {
                    // 增量查询数据
                    List<Map<String, String>> rows = getTableData(mysqlConnection, tableName, offset, batchSize, lastSyncTime);
                    if (rows.isEmpty()) {
                        hasMoreData = false; // 如果没有数据了，就结束分页
                    } else {
                        // 批量同步到 Redis
                        batchSyncToRedis(redisClient, tableName, rows);

                        // 更新最后同步时间
                        updateLastSyncTime(redisClient, tableName, rows);
                        offset += batchSize; // 下一次查询的起始位置
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // 获取最后同步的时间
    private static String getLastSyncTime(Jedis redisClient, String tableName) {
        String redisKey = tableName + ":last_sync_time";
        String lastSyncTime = redisClient.get(redisKey);
        return lastSyncTime != null ? lastSyncTime : "1970-01-01 00:00:00"; // 默认从时间戳0开始
    }

    // 更新最后同步的时间
    private static void updateLastSyncTime(Jedis redisClient, String tableName, List<Map<String, String>> rows) {
        // 假设表中的某个字段存储时间戳，我们选取最新的更新时间作为同步时间
        String latestTime = rows.stream()
                .map(row -> row.get("updated_at")) // 假设有updated_at字段
                .max(String::compareTo)
                .orElse("1970-01-01 00:00:00");

        String redisKey = tableName + ":last_sync_time";
        redisClient.set(redisKey, latestTime);
    }

    // 增量查询表数据
    private static List<Map<String, String>> getTableData(Connection mysqlConnection, String tableName, int offset, int limit, String lastSyncTime) throws SQLException {
        List<Map<String, String>> rows = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE updated_at > ? LIMIT ?, ?";
        try (PreparedStatement stmt = mysqlConnection.prepareStatement(query)) {
            stmt.setString(1, lastSyncTime);
            stmt.setInt(2, offset);
            stmt.setInt(3, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    String primaryKey = rs.getString(1); // 假设主键是第一列

                    // 跳过主键为空的行
                    if (primaryKey == null || primaryKey.trim().isEmpty()) {
                        continue;
                    }

                    // 处理当前行的数据
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rsMeta.getColumnName(i);
                        String value = rs.getString(i);
                        row.put(columnName, value != null ? value : ""); // 用空字符串代替null
                    }

                    rows.add(row);
                }
            }
        }
        return rows;
    }

    // 批量同步数据到 Redis 使用 Pipeline
    private static void batchSyncToRedis(Jedis redisClient, String tableName, List<Map<String, String>> rows) {
        Pipeline pipeline = redisClient.pipelined();
        Set<String> redisKeys = new HashSet<>();
        for (Map<String, String> row : rows) {
            String primaryKey = row.get("id"); // 假设主键字段名为 id

            // 跳过主键为空的行
            if (primaryKey == null || primaryKey.trim().isEmpty()) {
                continue;
            }

            String redisKey = tableName + ":" + primaryKey;

            // 检查 Redis 中是否存在该数据，如果存在则与 MySQL 数据进行对比
            if (redisKeys.contains(redisKey)) {
                // 已经同步过这条数据，跳过
                continue;
            }

            redisKeys.add(redisKey);

            // 检查 Redis 中是否有数据
            Map<String, String> redisData = redisClient.hgetAll(redisKey);
            boolean isDataDifferent = false;

            // 如果 Redis 中的数据与 MySQL 数据不同，则更新 Redis 中的数据
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String columnName = entry.getKey();
                String mysqlValue = entry.getValue();
                String redisValue = redisData.get(columnName);

                if (redisValue == null || !mysqlValue.equals(redisValue)) {
                    isDataDifferent = true;
                    break; // 数据发生了变化
                }
            }

            // 如果数据有变化，才更新 Redis
            if (isDataDifferent) {
                pipeline.hset(redisKey, row);
                pipeline.expire(redisKey, REDIS_KEY_EXPIRATION_TIME); // 设置过期时间
            }
        }

        // 执行 Pipeline 操作
        try {
            pipeline.sync(); // 执行所有命令
        } catch (JedisConnectionException | JedisDataException e) {
            System.err.println("Redis 批量同步失败，重试中...");
            retrySync(redisClient, pipeline); // 重试机制
        }
    }

    // 重试 Redis 同步操作
    private static void retrySync(Jedis redisClient, Pipeline pipeline) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                pipeline.sync(); // 尝试执行命令
                return; // 成功则退出
            } catch (JedisConnectionException | JedisDataException e) {
                retries++;
                try {
                    Thread.sleep(RETRY_DELAY_MS); // 延时重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.err.println("重试超过最大次数，Redis 同步失败！");
    }
}


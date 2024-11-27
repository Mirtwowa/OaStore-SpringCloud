package org.example.oastoreaop.service;
import redis.clients.jedis.Jedis;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MySQLToRedisSync {
    public static void main(String[] args) {
        // MySQL配置
        String mysqlUrl = "jdbc:mysql://localhost:3306/java";
        String mysqlUser = "root";
        String mysqlPassword = "123456";

        // Redis配置
        String redisHost = "localhost";
        int redisPort = 6379;

        // 连接MySQL和Redis
        try (Connection mysqlConnection = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
             Jedis redisClient = new Jedis(redisHost, redisPort)) {

            // 获取所有表名
            DatabaseMetaData metaData = mysqlConnection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("同步表：" + tableName);

                // 查询表中的所有数据
                String query = "SELECT * FROM " + tableName;
                try (Statement stmt = mysqlConnection.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    // 获取列信息
                    ResultSetMetaData rsMeta = rs.getMetaData();
                    int columnCount = rsMeta.getColumnCount();

                    // 同步数据到Redis
                    while (rs.next()) {
                        Map<String, String> row = new HashMap<>();
                        String primaryKey = rs.getString(1); // 获取主键值

                        // 跳过主键为null的行
                        if (primaryKey == null || primaryKey.trim().isEmpty()) {
                            System.out.println("跳过主键为空的行");
                            continue;
                        }

                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = rsMeta.getColumnName(i);
                            String value = rs.getString(i);
                            row.put(columnName, value != null ? value : ""); // 用空字符串代替null
                        }

                        // 使用Redis Hash存储表数据
                        String redisKey = tableName + ":" + primaryKey;
                        redisClient.hset(redisKey, row);
                    }

                }
            }
            System.out.println("数据同步完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package org.oastore.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.common.vo.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Repository
@Mapper
public interface UserMapper {
    @Async
    CompletableFuture<Integer> updateUserBalance(Map<String, Object> map);

    User getUser(String tableName, Integer userId);
}

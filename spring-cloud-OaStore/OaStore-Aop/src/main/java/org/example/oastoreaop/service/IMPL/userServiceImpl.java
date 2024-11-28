package org.example.oastoreaop.service.IMPL;

import org.example.oastoreaop.mapper.UserMapper;
import org.example.oastoreaop.service.UserService;
import org.example.oastoreaop.util.EncryptUtils;
import org.example.oastoreaop.util.JwtUtil;
import org.example.oastoreaop.util.ThreadLocalUtil;
import org.example.oastoreaop.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class userServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Override
    public User findByUserName(String account) {
        return userMapper.findByUserName(account);
    }

    /**
     * @param account 账号
     * @param password 密码
     * @param name 姓名
     * @param stu_id 学号
     */
    @Override
    public void insertUser(String account, String password, String name, String stu_id) {
        //加密
         String md5String = EncryptUtils.getMD5String(password);
        userMapper.insertUser(account,md5String,name,stu_id);
    }

    @Override
    public void update(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateUserIcon(String userIcon) {
        Map<String,Object> objectMap = ThreadLocalUtil.get();
        Integer id  = (Integer) objectMap.get("id");
        userMapper.updateUserIcon(userIcon,id);
    }

    @Override
    public void updatePwd(String newPwd,String account) {
        userMapper.updatePwd(EncryptUtils.getMD5String(newPwd),account);
    }
    @Async
    @Override
    public CompletableFuture<String> insertRedis(User loginuser) {
        Map<String,Object> claims =new HashMap<>();
        claims.put("id",loginuser.getId());
        claims.put("username",loginuser.getUsername());
        claims.put("password",loginuser.getPassword());
        //生成请求头
        String userKey = "user:" + claims.get("id");
        String token = JwtUtil.genToken(claims);
        ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
        operations.set(userKey,token,90, TimeUnit.DAYS);
        return CompletableFuture.completedFuture(token);
    }
}

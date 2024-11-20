package org.oastore.controller;

import org.example.common.vo.Product;
import org.example.common.vo.User;
import org.oastore.mapper.ProductMapper;
import org.oastore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PayController {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
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
    public User getUser(Integer userId,Integer productId) {

        Product amount = productMapper.findById(productId);
        String tableName = findTable(userId);
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("amount",amount.getPrice());
        map.put("tableName",tableName);
        userMapper.updateUserBalance(map);
        return userMapper.getUser(tableName, userId);
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
}

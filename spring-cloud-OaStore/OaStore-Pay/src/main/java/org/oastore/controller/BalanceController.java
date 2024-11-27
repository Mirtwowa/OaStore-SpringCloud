package org.oastore.controller;

import org.example.common.vo.Result;
import org.oastore.service.BalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);
    @Autowired
    private BalanceService balanceService;
    @Scheduled(cron = "0 0 0 * * ?") // 每天午夜 00:00 执行
    public void updateBalanceTask() {
        try {
            int effectRows = balanceService.insertFixedBalance();
            logger.info("每日餐补发放成功, 影响行数: {}", effectRows);
        } catch (Exception e) {
            logger.error("每日餐补发放失败", e);
        }
    }
}

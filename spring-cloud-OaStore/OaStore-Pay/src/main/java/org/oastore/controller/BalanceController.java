package org.oastore.controller;

import org.example.common.vo.Result;
import org.oastore.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {
    @Autowired
    private BalanceService balanceService;
    @PostMapping("/updateBalance")
    public Result updateBalance() {
        int effectRows = balanceService.insertFixedBalance();
        return Result.success("每日餐补发放成功",effectRows);
    }
}

package org.oastore.service;

import org.oastore.mapper.BalanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BalanceService {

    @Autowired
    private BalanceMapper balanceMapper;

    @Transactional
    public int insertFixedBalance() {
        int effectRows = 0;
        effectRows += balanceMapper.updateGeneralManagersBalance();
        effectRows += balanceMapper.updateDepartmentHeadsBalance();
        effectRows += balanceMapper.updateProjectManagersBalance();
        effectRows += balanceMapper.updateEmployeesBalance();
        return effectRows;
    }
}


package org.oastore.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface BalanceMapper {
    int updateGeneralManagersBalance();
    int updateDepartmentHeadsBalance();
    int updateProjectManagersBalance();
    int updateEmployeesBalance();
}

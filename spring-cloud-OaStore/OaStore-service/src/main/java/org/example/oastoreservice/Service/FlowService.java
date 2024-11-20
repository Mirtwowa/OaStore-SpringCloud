package org.example.oastoreservice.Service;

import org.example.oastoreservice.Flow;
import org.example.oastoreservice.Man;

import java.util.List;

public interface FlowService {
    List<Flow> getAllFlows();
    List<Flow> getFlowsByInitiatorId(int initiatorId);
    List<Flow> getFlowsByState(String state);
    List<Flow> getSubordinateFlows(String auditorId);


    void addFlow(String type, int initiator, String content, String remark);

    void updateAuditor(int flow, String state, String newauditor);
    List<Flow> getFlowsToAuditBySuperiorId(int superiorId);



    // 新增方法：通过用户 ID 查询其所有下属
    List<Man> getAllSubordinates(int id);


    void handleApproval(int flowId, int auditorId, String decision);
}

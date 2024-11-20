package org.example.oastoreservice.Service.impl;

import org.example.oastoreservice.Flow;
import org.example.oastoreservice.Man;
import org.example.oastoreservice.Mapper.FlowMapper;
import org.example.oastoreservice.Service.FlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class FlowServiceImpl implements FlowService {

    private final FlowMapper flowMapper;

    @Autowired
    public FlowServiceImpl(FlowMapper flowMapper) {
        this.flowMapper = flowMapper;
    }

    @Override
    public List<Flow> getAllFlows() {
        return flowMapper.selectAll();
    }

    @Override
    public List<Flow> getFlowsByInitiatorId(int initiatorId) {
        return flowMapper.findByInitiatorId(initiatorId);
    }

    @Override
    public List<Flow> getFlowsByState(String state) {
        return flowMapper.findByState(state);
    }

    @Override
    public List<Flow> getSubordinateFlows(String auditorId) {
        return flowMapper.findSubordinateFlows(auditorId);
    }


    @Override
    public void addFlow(String type, int initiator,  String content, String remark) {
        flowMapper.insertFlow(type, initiator, String.valueOf(initiator), content, remark);
    }







    @Override
    public void updateAuditor(int flow, String state, String newauditor) {
        flowMapper.updateAuditorIdAndAuditTime(flow, state, newauditor);
    }


    @Override
    public List<Man> getAllSubordinates(int id) {
        // 获取 ID 的首位字符
        char idPrefix = String.valueOf(id).charAt(0);

        List<Man> subordinates = new ArrayList<>();

        // 根据身份查询下属
        switch (idPrefix) {
            case '1': // 总经理
                return flowMapper.findDepartment(id);

            case '2': // 部门主管
                return flowMapper.findProjectManagers(id);

            case '3': // 项目经理
                return flowMapper.findEmployees(id);

            default:
                throw new IllegalArgumentException("Invalid ID: No subordinates found for this ID.");
        }
    }



    @Override
    public List<Flow> getFlowsToAuditBySuperiorId(int superiorId) {
        // 1. 获取所有下属
        List<Man> subordinates = flowMapper.findAllSubordinates(superiorId);

        // 2. 提取下属 ID 的最后 8 位
        List<String> subordinateIds = subordinates.stream()
                .map(man -> String.format("%08d", man.getId())) // 确保下属 ID 转为 8 位字符串
                .collect(Collectors.toList());

        // 3. 查询符合条件的流程
        return flowMapper.findFlowsByAuditorIdsAndState(subordinateIds);
    }

    @Override
    public void handleApproval(int flowId, int auditorId, String decision) {
        // 获取当前流程
        Flow flow = flowMapper.findFlowById(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("流程不存在");
        }

        // 如果决策是 "No"，直接更新状态为 Reject
        if ("No".equalsIgnoreCase(decision)) {
            flowMapper.rejectFlow(flowId);
            return;
        }

        // 判断类型是否为 "请假" 或 "出差"
        String type = flow.getType();
        if (!"请假".equals(type) && !"出差".equals(type)) {
            throw new IllegalArgumentException("不支持的流程类型：" + type);
        }

        // 获取内容字段中的天数
        int days;
        try {
            days = Integer.parseInt(flow.getContent()); // 假设天数在内容字段中
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("流程内容中不包含有效的天数: " + flow.getContent());
        }

        // 动态分配下一审核人
        int nextAuditorId = determineNextAuditorId(flow.getInitiator(), days);

        // 如果没有下一个审核人，则流程完成
        if (nextAuditorId == auditorId) {
            flowMapper.updateFlowStateAndAuditor(flowId, "Pass", String.valueOf(auditorId));
        } else {
            // 否则更新为 Under Review
            flowMapper.updateFlowStateAndAuditor(flowId, "Under Review", String.valueOf(auditorId));
        }
    }

    private int determineNextAuditorId(int currentAuditorId, int days) {
        try {
            if (days <= 3) {
                Man projectManager = flowMapper.findProjectManagerById(currentAuditorId);
                return projectManager.getSuperior() ;
            } else if (days <= 7) {
                Man departmentHead = flowMapper.findDepartmentHeadById(flowMapper.findProjectManagerById(currentAuditorId).getSuperior());
                return departmentHead.getSuperior(); // 部门经理
            } else {
                Integer projectManagerId = flowMapper.findProjectManagerById(currentAuditorId).getSuperior();
                Integer departmentManagerId = flowMapper.findDepartmentHeadById(projectManagerId).getSuperior();
                Man generalManager = flowMapper.findGeneralManagerById(departmentManagerId);
                return  generalManager.getSuperior(); // 总经理
            }
        }catch (Exception e){
            throw new RuntimeException("未找到责任链终端");
        }
        // 动态分配下一审核人
    }




}
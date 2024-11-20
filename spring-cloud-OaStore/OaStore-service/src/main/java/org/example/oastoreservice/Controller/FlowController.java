package org.example.oastoreservice.Controller;

import org.example.common.vo.Result;
        import org.example.oastoreservice.Flow;
        import org.example.oastoreservice.Man;
        import org.example.oastoreservice.Mapper.FlowMapper;
        import org.example.oastoreservice.Service.FlowService;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.data.relational.core.sql.In;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;

        import java.util.List;


@RestController
@RequestMapping("/flows")
public class FlowController {

    private final FlowService flowService;

    @Autowired
    public FlowController(FlowService flowService) {
        this.flowService = flowService;
    }

    @GetMapping("/allflow")//所有
    public List<Flow> getAllFlows() {
        return flowService.getAllFlows();
    }

    @GetMapping("/initiator/{initiatorId}")//通过申请人查询
    public List<Flow> getFlowsByInitiatorId(@PathVariable int initiatorId) {
        return flowService.getFlowsByInitiatorId(initiatorId);
    }
    @GetMapping("/subordinates")//通过审核人查询
    public List<Flow> getSubordinateFlows(@RequestParam String auditorId) {
        return flowService.getSubordinateFlows(auditorId);
    }
    @GetMapping("/subordinate")
    public List<Man> getAllSubordinates(@RequestParam int id) {
        List<Man> subordinates = flowService.getAllSubordinates(id);
        return subordinates;
    }
    @GetMapping("/state/{state}")//通过状态查询
    public List<Flow> getFlowsByState(@PathVariable String state) {
        return flowService.getFlowsByState(state);
    }

    @GetMapping("/to-audit")
    public List<Flow> getFlowsToAudit(@RequestParam int superiorId) {
        return flowService.getFlowsToAuditBySuperiorId(superiorId);
    }

    @PostMapping("/add")//申请
    public ResponseEntity<String> addFlow(@RequestBody Flow flow) {
        try {
            flowService.addFlow(flow.getType(), flow.getInitiator(), flow.getContent(), flow.getRemark());
            return ResponseEntity.ok("流程插入成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("流程插入失败: " + e.getMessage());
        }
    }

    @PostMapping("/handle")
    public Result handleApproval(
            @RequestParam int flow,
            @RequestParam Integer auditor,
            @RequestParam String decision) {
        try {
            flowService.handleApproval(flow, auditor, decision);
            return Result.success("审批已处理",null);
        } catch (Exception e) {
            return Result.error(504,e.getMessage());
        }
    }
    @PutMapping("/updateAuditor")//审核
    public ResponseEntity<String> updateAuditor(@RequestParam int flow,
                                                @RequestParam String newauditor,
                                                @RequestParam String state) {
        try {
            flowService.updateAuditor(flow, state, newauditor);
            return ResponseEntity.ok("审核人ID和审核时间更新成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新失败: " + e.getMessage());
        }
    }
}
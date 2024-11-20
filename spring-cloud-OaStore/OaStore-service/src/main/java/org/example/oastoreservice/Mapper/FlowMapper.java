
package org.example.oastoreservice.Mapper;

        import org.apache.ibatis.annotations.*;
        import org.example.oastoreservice.Flow;
        import org.example.oastoreservice.Man;

        import java.util.Collection;
        import java.util.List;

@Mapper
public interface FlowMapper {
    @Select("SELECT * FROM `approval_flows`")
    List<Flow> selectAll();

    @Select("SELECT * FROM approval_flows WHERE initiator = #{initiatorId}")
    List<Flow> findByInitiatorId(int initiatorId);


    @Select("SELECT * FROM approval_flows WHERE state = #{state}")
    List<Flow> findByState(String state);
    @Select("SELECT * FROM employees WHERE superior = #{superior}")
    List<Man> findEmployees(int superior);//查询下属职员
    @Select("SELECT * FROM project_managers WHERE superior = #{superior}")
    List<Man> findProjectManagers(int superior);//查询下属经理
    @Select("SELECT * FROM department_heads WHERE superior = #{superior}")
    List<Man> findDepartment(int superior);//查询下属主管
    @Select("SELECT * FROM approval_flows WHERE RIGHT(auditor, 8) = #{auditorId}")
    List<Flow> findSubordinateFlows(@Param("auditorId") String auditorId);
    @Insert("insert into approval_flows(type,initiator,auditor,content,remark,addtime)"+
            "values (#{type},#{initiator},#{auditor},#{content},#{remark},now())")
    void insertFlow(String type,int initiator,String auditor,String content,String remark);


    @Select({
            "SELECT * FROM employees WHERE superior = #{superior}",
            "UNION ALL",
            "SELECT * FROM project_managers WHERE superior = #{superior}",
            "UNION ALL",
            "SELECT * FROM department_heads WHERE superior = #{superior}"
    })
    List<Man> findAllSubordinates(int superior);

    @Select({
            "<script>",
            "SELECT * FROM approval_flows",
            "WHERE RIGHT(auditor, 8) IN",
            "<foreach collection='auditorIds' item='auditorId' open='(' separator=',' close=')'>",
            "#{auditorId}",
            "</foreach>",
            "AND state IN ('Under Review', 'Not Reviewed')",
            "</script>"
    })
    List<Flow> findFlowsByAuditorIdsAndState(@Param("auditorIds") List<String> auditorIds);


    @Update("UPDATE approval_flows SET auditor = CONCAT(COALESCE(auditor, ''), #{newauditor}),state = #{state},audittime = NOW() WHERE flow = #{flow}")
    void updateAuditorIdAndAuditTime(@Param("flow") int flow,@Param("state") String state ,@Param("newauditor") String newauditor);

    @Select("SELECT * FROM `approval_flows` WHERE flow = #{flow}")
    Flow findFlowById(int flow);

    @Update("UPDATE approval_flows SET state = #{state}, auditor = CONCAT(COALESCE(auditor, ''), #{auditor}), audittime = NOW() WHERE flow = #{flowId}")
    void updateFlowStateAndAuditor(@Param("flowId") int flowId, @Param("state") String state, @Param("auditor") String auditor);

    @Update("UPDATE approval_flows SET state = 'Reject', audittime = NOW() WHERE flow = #{flowId}")
    void rejectFlow(@Param("flowId") int flowId);

    @Select("SELECT * FROM employees WHERE id = #{id}")
    Man findProjectManagerById(int id);

    @Select("SELECT * FROM department_heads WHERE id = #{id}")
    Man findGeneralManagerById(int id);

    @Select("SELECT * FROM project_managers WHERE id = #{id}")
    Man findDepartmentHeadById(int id);
}

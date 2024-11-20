package org.example.oastoreservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Flow {
    private int flow;
    private String type;
    private int initiator;
    private String auditor;
    private String state;
    private String content;
    private String remark;
    private Time addtime;
    private Time audittime;
}

package org.example.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private Long superiorId;
    private String name;
    private String tel;
    private String email;
    private String department;
    private Float balance;
    private LocalDateTime createdAt;
}

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
    private String username;
    @JsonIgnore
    private String password;
    private String name;
    private String userPic;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String stu_id;
}

package org.example.oastoreservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@AllArgsConstructor
public class Man {
    private Integer id;
    private Integer superior;
    private String name;
    private String department;
    private String email;
    private String tel;
    private Float balance;
}

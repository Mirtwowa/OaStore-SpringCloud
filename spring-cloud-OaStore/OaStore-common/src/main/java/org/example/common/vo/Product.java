package org.example.common.vo;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {
    private Integer id;
    private String name;
    private String category;
    private String description;
    private double price;
    private int stockQuantity;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

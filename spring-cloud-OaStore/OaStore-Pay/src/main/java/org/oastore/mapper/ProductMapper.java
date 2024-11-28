package org.oastore.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.common.vo.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ProductMapper {
    List<Product> findAll();
    List<Product> getFoodCourtData();
    List<Product> getCoffeeShopData();
    Product findById(Integer id);
    int insert(Product product);
    int update(Product product);
    int delete(Integer id);
    int Deduction(Integer id);
}

package com.keykiosk.Models.Repository;


import com.keykiosk.Models.Entity.CategoryEntity;
import com.keykiosk.Models.Entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByCategory_CategoryId(Long categoryId);

    ProductEntity findByNameProduct(String productName);


}

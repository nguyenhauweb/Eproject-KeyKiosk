package com.keykiosk.Models.Repository;

import com.keykiosk.Models.Entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long>, JpaSpecificationExecutor<CategoryEntity> {

    CategoryEntity findByName(String entertainment);

    List<CategoryEntity> findAllByName(String name);
}

package com.keykiosk.Models.Repository;

import com.keykiosk.Models.Entity.CategoryEntity;
import com.keykiosk.Models.Entity.ImageEntity;

import com.keykiosk.Models.Entity.ProductEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long>, JpaSpecificationExecutor<ImageEntity> {

    ImageEntity findByCategoryAndImageType(CategoryEntity categoryEntity, ImageType imageType);

    ImageEntity findByProductAndImageType(ProductEntity productEntity, ImageType imageType);

    ImageEntity findByAccountAndImageType(UserEntity loggedInUser, ImageType imageType);
}
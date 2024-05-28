package com.keykiosk.Models.Repository;

import com.keykiosk.Models.Entity.SoftwareAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface SoftwareAccountRepository extends JpaRepository<SoftwareAccountEntity, Long> {

    @Query("SELECT COUNT(sa) FROM SoftwareAccountEntity sa WHERE sa.product.nameProduct = :nameProduct AND sa.deleted = false")
    int countByNameProduct(@Param("nameProduct") String nameProduct);

    List<SoftwareAccountEntity> findAllByAccountInfo(String accountInfo);

    List<SoftwareAccountEntity> findAllByAccountInfoContaining(String keyword);


    @Query("SELECT s FROM SoftwareAccountEntity s ORDER BY s.createdAt DESC")
    List<SoftwareAccountEntity> findAllSortedByCreationDate();


    List<SoftwareAccountEntity> findByProduct_NameProductOrderBySoftwareAccountIdAsc(String nameProduct);

    List<SoftwareAccountEntity> findByProduct_ProductIdAndDeleted(Long productId, boolean deleted);

    List<SoftwareAccountEntity> findByProduct_NameProductAndDeletedFalse(String nameProduct);
}

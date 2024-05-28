package com.keykiosk.Models.Repository;

import com.keykiosk.Models.Entity.SoftwareAccountEntity;
import com.keykiosk.Models.Entity.SoftwareLicenseKeyEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SoftwareLicenseKeyRepository extends JpaRepository<SoftwareLicenseKeyEntity, Long>, JpaSpecificationExecutor<SoftwareLicenseKeyEntity> {

    @Query("SELECT COUNT(sl) FROM SoftwareLicenseKeyEntity sl WHERE sl.product.nameProduct = :nameProduct AND sl.deleted = false")
    int countByNameProduct(@Param("nameProduct") String nameProduct);

    List<SoftwareLicenseKeyEntity> findAllByLicenseKey(String stringCellValue);

    @Query("SELECT s FROM SoftwareLicenseKeyEntity s ORDER BY s.createdAt DESC")
    List<SoftwareLicenseKeyEntity> findAllSortedByCreationDate();

    List<SoftwareLicenseKeyEntity> findByProduct_NameProductOrderByLicenseIdAsc(String nameProduct);

    List<SoftwareLicenseKeyEntity> findByProduct_ProductIdAndDeleted(Long productId, boolean deleted);

    List<SoftwareLicenseKeyEntity> findByProduct_NameProductAndDeletedFalse(String nameProduct);
}

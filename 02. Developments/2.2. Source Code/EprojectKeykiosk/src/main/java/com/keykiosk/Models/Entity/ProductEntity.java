package com.keykiosk.Models.Entity;

import com.keykiosk.Models.EnumType.ProductStatus;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private UserEntity account;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "name_product", nullable = false, unique = true)
    private String nameProduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductTypeCode productType;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @OneToMany(mappedBy = "product")
    private List<SoftwareAccountEntity> softwareAccounts;

    @OneToMany(mappedBy = "product")
    private List<SoftwareLicenseKeyEntity> softwareLicenseKeys;

}

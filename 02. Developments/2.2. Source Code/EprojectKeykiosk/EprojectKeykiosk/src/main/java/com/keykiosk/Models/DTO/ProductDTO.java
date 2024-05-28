package com.keykiosk.Models.DTO;

import com.keykiosk.Models.EnumType.ImageType;
import com.keykiosk.Models.EnumType.ProductStatus;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
    private Long productId;

    private Long accountId;

    private String nameProduct;
    private ProductTypeCode productType;
    private Long categoryId;
    private Long userId;

    private String categoryName;


    private String description;

    private BigDecimal price;

    private Integer quantity;

    private ProductStatus status;

    private Long imageId;

    private String imageUrl;

    private ImageType imageType;
    public String getCategory() {
        return categoryName;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "quantity=" + quantity +
                ", productId=" + productId +
                ", nameProduct='" + nameProduct + '\'' +
                '}';
    }
}

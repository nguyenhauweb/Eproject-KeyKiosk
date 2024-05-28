package com.keykiosk.Models.DTO;


import com.keykiosk.Models.Entity.ProductEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.OrderStatus;
import com.keykiosk.Models.EnumType.PaymentMethod;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {

    private Long orderId;
    private String codeId;
    private Long productId;
    private Long accountId;

    private Integer quantity;
    private ProductTypeCode productType;

    private PaymentMethod paymentMethod;
    private String nameProduct;

    private BigDecimal totalAmount;


    private OrderStatus orderStatus;


    private LocalDateTime orderDate;

    public String getOrderDateFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a M/d/yyyy");
        return orderDate.format(formatter);
    }

}

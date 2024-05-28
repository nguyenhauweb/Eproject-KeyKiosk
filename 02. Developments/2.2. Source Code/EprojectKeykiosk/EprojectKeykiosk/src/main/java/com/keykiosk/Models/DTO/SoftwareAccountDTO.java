package com.keykiosk.Models.DTO;

import com.keykiosk.Models.EnumType.PaymentStatus;
import com.keykiosk.Models.EnumType.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SoftwareAccountDTO {

    private Long softwareAccountId;

    private String nameProduct;

    private Long accountId;

    private Long productId;

    private String accountInfo;
private String codeId;
    private Status status;

    private PaymentStatus paymentStatus;

    @Override
    public String toString() {
        return "SoftwareAccountDTO{" +
                "accountInfo='" + accountInfo + '\'' +
                '}';
    }


}

package com.keykiosk.Models.DTO;

import com.keykiosk.Models.EnumType.PaymentStatus;
import com.keykiosk.Models.EnumType.Status;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SoftwareLicenseKeyDTO {
    @Getter
    private Long licenseId;


    private Long productId;

    private String licenseKey;

    private Status status;

    private String nameProduct;
    private String codeId;
    private PaymentStatus paymentStatus;

    private Long accountId;

private boolean deleted;
    @Override
    public String toString() {
        return "SoftwareLicenseKeyDTO{" +
                "licenseId=" + licenseId +
                ", licenseKey='" + licenseKey + '\'' +
                ", status=" + status +
                ", paymentStatus=" + paymentStatus +
                '}';
    }

}

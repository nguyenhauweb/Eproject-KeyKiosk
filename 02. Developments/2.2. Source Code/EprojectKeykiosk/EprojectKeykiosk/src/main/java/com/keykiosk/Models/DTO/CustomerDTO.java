package com.keykiosk.Models.DTO;

import com.keykiosk.Models.EnumType.GenderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDTO {
    private Long customerId;

    private String fullName;

    private String email;

    private GenderType gender;

    private String phoneNumber;

    private String address;

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "fullName='" + fullName + '\'' +
                '}';
    }
}

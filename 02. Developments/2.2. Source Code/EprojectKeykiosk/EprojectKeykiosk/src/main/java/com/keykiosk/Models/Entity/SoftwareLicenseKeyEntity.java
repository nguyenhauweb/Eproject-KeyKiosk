package com.keykiosk.Models.Entity;

import com.keykiosk.Models.EnumType.PaymentStatus;
import com.keykiosk.Models.EnumType.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "SoftwareLicenseKeys")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SoftwareLicenseKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Long licenseId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private UserEntity account;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "license_key", nullable = false)
    private String licenseKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "code_id", nullable = false)
    private String codeId;

//    @CreationTimestamp
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime creationDate;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

}
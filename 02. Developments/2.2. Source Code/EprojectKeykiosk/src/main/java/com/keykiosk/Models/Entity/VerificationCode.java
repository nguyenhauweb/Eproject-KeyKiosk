package com.keykiosk.Models.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_code_id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "code")
    private String code;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private UserEntity user;

}
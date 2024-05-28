package com.keykiosk.Models.Entity;

import com.keykiosk.Models.EnumType.Role;
import com.keykiosk.Models.EnumType.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Accounts")
@Data
@AllArgsConstructor
public class UserEntity {

    public UserEntity() {
        this.balance = new BigDecimal(0);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(columnDefinition = "NVARCHAR(255)", name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VerificationCode> verificationCodes;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    public void setUsername(String username) {
        if (this.username != null && this.username.equals("admin")) {
            throw new IllegalArgumentException("Cannot change the username of the admin user");
        }
        this.username = username;
    }

    public void setRole(Role role) {
        if (this.username != null && this.username.equals("admin")) {
            throw new IllegalArgumentException("Cannot change the role of the admin user");
        }
        this.role = role;
    }

    public void setStatus(Status status) {
        if (this.username != null && this.username.equals("admin")) {
            throw new IllegalArgumentException("Cannot change the status of the admin user");
        }
        this.status = status;
    }


}
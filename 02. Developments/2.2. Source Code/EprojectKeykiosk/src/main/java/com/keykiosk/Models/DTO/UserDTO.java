package com.keykiosk.Models.DTO;

import com.keykiosk.Models.EnumType.ImageType;
import com.keykiosk.Models.EnumType.Role;
import com.keykiosk.Models.EnumType.Status;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    @Getter
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9])(?!.*\\s).{8,15}$", message = "Password needs: 1 uppercase, 1 lowercase, 1 number, and 1 special character.")
    private String passwordHash;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @DecimalMin(value = "0.0", message = "Balance must be positive")
    private BigDecimal balance;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Status is required")
    private Status status;

    private Long imageId;
    private String imageUrl;
    private ImageType imageType;


//    public static UserDTO createUser(String email, String username, String passwordHash, String fullName, Role role, Status status) {
//        return new UserDTO(null, email, username, passwordHash, fullName, BigDecimal.ZERO, role, status);
//    }

    public static Role getRoleFromComboBox(String role) {
        return role.equals(Role.ADMIN.name()) ? Role.ADMIN : role.equals(Role.SELLER.name()) ? Role.SELLER : Role.CLIENT;
    }

    public static Status getStatusFromComboBox(String status) {
        return status.equals(Status.ACTIVE.name()) ? Status.ACTIVE : Status.INACTIVE;
    }

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

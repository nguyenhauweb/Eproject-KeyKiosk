package com.keykiosk.Models.Entity;

import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.EnumType.Role;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        Optional<UserDTO> existingAdmin = Optional.ofNullable(userService.findByUsername("admin"));
        if (existingAdmin.isEmpty()) {
            String encodedPassword = passwordEncoder.encode("123");
            UserDTO admin = UserDTO.builder()
                    .email("admin")
                    .username("admin")
                    .passwordHash(encodedPassword)
                    .fullName("admin")
                    .role(Role.ADMIN)
                    .status(Status.ACTIVE)
                    .build();
//            UserDTO admin = UserDTO.createUser("admin", "admin", encodedPassword, "admin", Role.ADMIN, Status.ACTIVE);
            userService.createUser(admin);
        }

        Optional<UserDTO> existingSeller = Optional.ofNullable(userService.findByUsername("seller"));
        if (existingSeller.isEmpty()) {
            String encodedPassword = passwordEncoder.encode("123");
            UserDTO seller = UserDTO.builder()
                    .email("seller")
                    .username("seller")
                    .passwordHash(encodedPassword)
                    .fullName("seller")
                    .role(Role.SELLER)
                    .status(Status.ACTIVE)
                    .build();
//            UserDTO seller = UserDTO.createUser("seller", "seller", encodedPassword, "seller", Role.SELLER, Status.ACTIVE);
            userService.createUser(seller);
        }
    }
}
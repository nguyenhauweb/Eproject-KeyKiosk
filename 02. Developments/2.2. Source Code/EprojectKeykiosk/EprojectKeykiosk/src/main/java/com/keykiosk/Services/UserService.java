package com.keykiosk.Services;
import com.keykiosk.Exception.RegistrationException;
import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.Entity.UserEntity;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO findByUsername(String username);
    Optional<UserDTO> getUserByUsername(String username);
    void addVerificationCodeAndSendEmail(String email) throws RegistrationException;
    Optional<UserEntity> login(String username, String password) throws RegistrationException;
    UserEntity getUserEntityById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO createUser(UserDTO userDTO) throws RegistrationException;
    void updateUser(UserDTO userToUpdate) throws RegistrationException;
    void deleteUserById(Long id);
    boolean verifyConfirmationCode(String email, String verificationCode);
    void addUser(UserDTO userDTO) throws RegistrationException;
    void deleteUser(UserDTO user);
    List<UserDTO> searchUsers(String keyword);
    UserEntity findUserByUsername(String username);
    void updateFullName(Long userId, String fullName) throws RegistrationException;
    Optional<Object> findById(long l);

    List<UserDTO> searchUsersByEmail(String keyword);

    List<UserDTO> searchUsersByUsername(String keyword);

    List<UserDTO> searchUsersByFullName(String keyword);

    void changePassword(UserEntity user, String currentPassword, String newPassword);

    void insertImageProfile( File selectedFile) throws Exception;
    List<UserDTO> getImageUrlForUser();

    UserEntity findByEmail(String email);
}
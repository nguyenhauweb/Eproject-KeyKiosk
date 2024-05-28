package com.keykiosk.Services.Impl;

import com.keykiosk.Config.ConfigUrl;
import com.keykiosk.Exception.RegistrationException;
import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.Entity.ImageEntity;
import com.keykiosk.Models.Entity.ProductEntity;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.Entity.VerificationCode;
import com.keykiosk.Models.EnumType.ImageType;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Models.Repository.ImageRepository;
import com.keykiosk.Models.Repository.UserRepository;
import com.keykiosk.Models.Repository.VerificationCodeRepository;
import com.keykiosk.Observer.UserObservable;
import com.keykiosk.Services.EmailService;
import com.keykiosk.Services.UserService;
import com.keykiosk.Util.FileUtil;
import com.keykiosk.Util.RandomCodeUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.keykiosk.Util.ImageUtils.createImageEntity;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private static int failedLoginAttempts = 0;
    private static LocalDateTime lastFailedLoginTime;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(5);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static void updateLastFailedLoginTime() {
        lastFailedLoginTime = LocalDateTime.now();
    }

    @Autowired
    private UserObservable userObservable;
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, VerificationCodeRepository verificationCodeRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.verificationCodeRepository = verificationCodeRepository;
        this.emailService = emailService;
    }

    @Override
    public UserDTO findByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username);
        if (userEntity != null) {
            return modelMapper.map(userEntity, UserDTO.class);
        } else {
            return null;
        }
    }

    @Override
    public Optional<UserDTO> getUserByUsername(String username) {
        if (username != null && !username.isEmpty()) {
            UserEntity userEntity = userRepository.findByUsername(username);
            return Optional.ofNullable(modelMapper.map(userEntity, UserDTO.class));
        }
        return Optional.empty();
    }

    @Override
    public void addVerificationCodeAndSendEmail(String email) throws RegistrationException {

        // Thêm mã xác minh vào cơ sở dữ liệu
        String verificationCode = RandomCodeUtil.generateRandomCode(6);
        VerificationCode code = new VerificationCode();
        code.setEmail(email);
        code.setCode(verificationCode);
        verificationCodeRepository.save(code);

        // Gửi mã xác nhận qua email
        emailService.sendVerificationEmailAsync(email, verificationCode);
    }

    // Perform login with the given username and password
    @Override
    public Optional<UserEntity> login(String username, String password) throws RegistrationException {
        Optional<UserEntity> optionalAccount = Optional.ofNullable(userRepository.findByUsername(username));
        if (optionalAccount.isPresent()) {
            UserEntity userEntity = optionalAccount.get();
            if (userEntity.getStatus() == Status.ACTIVE) {
                // Kiểm tra mật khẩu bằng cách sử dụng mã hóa bcrypt
                if (encoder.matches(password, userEntity.getPasswordHash())) {
                    // Reset số lần đăng nhập sai khi đăng nhập thành công
                    failedLoginAttempts = 0;
                    return Optional.of(userEntity);
                } else {
                    // Xử lý khi mật khẩu không đúng
                    handleFailedLogin();
                    throw new RegistrationException("Invalid username or password!");
                }
            } else if (userEntity.getStatus() == Status.INACTIVE) {
                throw new RegistrationException("Your account has been locked. Please try again later.");
            } else {
                throw new RegistrationException("Unknown account status. Please contact support.");
            }
        } else {
            // Xử lý khi không tìm thấy tài khoản
            handleFailedLogin();
            throw new RegistrationException("Invalid username or password!");
        }
    }

    private void handleFailedLogin() {
        failedLoginAttempts++;
        updateLastFailedLoginTime(); // Cập nhật thời gian lần đăng nhập sai cuối cùng
        if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            updateLastFailedLoginTime(); // Cập nhật thời gian lần đăng nhập sai cuối cùng
        }
    }

    public static boolean isAccountLocked() {
        if (failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            if (lastFailedLoginTime != null && Duration.between(lastFailedLoginTime, LocalDateTime.now()).compareTo(LOCKOUT_DURATION) < 0) {
                return true; // Tài khoản đã bị khóa
            } else {
                // Reset lại số lần đăng nhập sai và thời gian
                failedLoginAttempts = 0;
                lastFailedLoginTime = null;
            }
        }
        return false;
    }

    @Override
    public UserEntity getUserEntityById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll();
        List<UserDTO> userDTOs = userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .collect(Collectors.toList());

        // Log data
        userDTOs.forEach(userDTO -> System.out.println(userDTO.toString()));

        return userDTOs;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) throws RegistrationException {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RegistrationException("Email is already in use: " + userDTO.getEmail());
        }
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RegistrationException("Username is already in use: " + userDTO.getUsername());
        }
        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
        return modelMapper.map(userRepository.save(userEntity), UserDTO.class);
    }
    @Override
    public void updateFullName(Long userId, String fullName) throws RegistrationException {
        UserEntity existingUser = userRepository.findById(userId).orElse(null);
        if (existingUser != null) {
            existingUser.setFullName(fullName);
            userRepository.save(existingUser);
        } else {
            throw new RegistrationException("User not found with id: " + userId);
        }
    }
    @Override
    public void updateUser(UserDTO userToUpdate) throws RegistrationException {
        UserEntity existingUser = userRepository.findById(userToUpdate.getId()).orElse(null);
        if (existingUser != null) {
            if (!existingUser.getEmail().equals(userToUpdate.getEmail()) && userRepository.existsByEmail(userToUpdate.getEmail())) {
                throw new RegistrationException("Email is already in use: " + userToUpdate.getEmail());
            }
            // Use ModelMapper to map the updated fields from UserDTO to the existing UserEntity
            modelMapper.map(userToUpdate, existingUser);
            userRepository.save(existingUser);
            userObservable.notifyUser();
        } else {
            throw new RegistrationException("User not found with id: " + userToUpdate.getId());
        }
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean verifyConfirmationCode(String email, String verificationCode) {
        // Kiểm tra xác nhận mã từ cơ sở dữ liệu
        Optional<VerificationCode> optionalCode = verificationCodeRepository.findByEmailAndCode(email, verificationCode);
        if (optionalCode.isPresent()) {
            // Xóa mã xác nhận sau khi đã xác nhận thành công
            verificationCodeRepository.delete(optionalCode.get());
            return true;
        }
        return false;
    }

    @Override
    public void addUser(UserDTO userDTO) throws RegistrationException {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RegistrationException("Username already exists: " + userDTO.getUsername());
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RegistrationException("Email already exists: " + userDTO.getEmail());
        }

        // Mã hóa mật khẩu trước khi lưu vào cơ sở dữ liệu
        userDTO.setPasswordHash(encoder.encode(userDTO.getPasswordHash()));

        // Lưu thông tin người dùng vào cơ sở dữ liệu
        UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
        userRepository.save(userEntity);
    }

    @Override
    public void deleteUser(UserDTO user) {
        if (user.getUsername().equals("admin")) {
            throw new IllegalArgumentException("Cannot delete the admin user");
        }
        userRepository.deleteById(user.getId());
    }

    @Override
    public List<UserDTO> searchUsers(String keyword) {
        Specification<UserEntity> spec = Specification
                .<UserEntity>where((root, query, cb) -> cb.like(root.get("username"), "%" + keyword + "%"))
                .or((root, query, cb) -> cb.like(root.get("email"), "%" + keyword + "%"))
                .or((root, query, cb) -> cb.like(root.get("fullName"), "%" + keyword + "%"));

        List<UserEntity> userEntities = userRepository.findAll(spec);
        return userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserEntity findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<Object> findById(long l) {
        Optional<UserEntity> userEntity = userRepository.findById(l);
        return userEntity.map(entity -> modelMapper.map(entity, UserDTO.class));
    }

    @Override
    public List<UserDTO> searchUsersByEmail(String keyword) {
        Specification<UserEntity> spec = (root, query, cb) -> cb.like(root.get("email"), "%" + keyword + "%");
        List<UserEntity> userEntities = userRepository.findAll(spec);
        return userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsersByUsername(String keyword) {
        Specification<UserEntity> spec = (root, query, cb) -> cb.like(root.get("username"), "%" + keyword + "%");
        List<UserEntity> userEntities = userRepository.findAll(spec);
        return userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsersByFullName(String keyword) {
        Specification<UserEntity> spec = (root, query, cb) -> cb.like(root.get("fullName"), "%" + keyword + "%");
        List<UserEntity> userEntities = userRepository.findAll(spec);
        return userEntities.stream()
                .map(userEntity -> modelMapper.map(userEntity, UserDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    public void changePassword(UserEntity user, String currentPassword, String newPassword) {
        if (encoder.matches(currentPassword, user.getPasswordHash())) {
            user.setPasswordHash(encoder.encode(newPassword));
            userRepository.save(user);
        }

    }

    @Override
    public void insertImageProfile(File selectedFile) throws Exception {
        File destinationFile = FileUtil.prepareDestinationFile(selectedFile, ConfigUrl.BASE_IMAGE_DIR + "/Account");

        // Get the current logged in user
        UserEntity currentUser = Model.getInstance().getLoggedInUser();

        // Find the current ImageEntity of the user
        ImageEntity currentImageEntity = imageRepository.findByAccountAndImageType(currentUser, ImageType.ACCOUNT_IMAGE);

        // If the user has a current image, delete it from the database and the file system
        if (currentImageEntity != null) {
            // Delete the image file
            Files.deleteIfExists(Paths.get(currentImageEntity.getImageUrl()));
            // Delete the ImageEntity from the database
            imageRepository.delete(currentImageEntity);
        }

        // Create a new ImageEntity and save it
        ImageEntity newImageEntity = createImageEntity(currentUser, destinationFile, currentUser);
        imageRepository.save(newImageEntity);

        // Copy the image file
        Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public List<UserDTO> getImageUrlForUser() {
        // Get the current logged in user
        UserEntity currentUser = Model.getInstance().getLoggedInUser();

        // Convert the logged in user to UserDTO with image
        UserDTO userDTO = convertToDTOWithImage(currentUser);

        // Debug: Print out the UserDTO's ID and Image URL
        System.out.println("UserDTO ID: " + userDTO.getId() + ", Image URL: " + userDTO.getImageUrl());

        // Return a list containing only the logged in user's UserDTO
        return Collections.singletonList(userDTO);
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);

    }


    private UserDTO convertToDTOWithImage(UserEntity userEntity) {
        UserDTO userDTO = convertToDTO(userEntity);
        ImageEntity imageEntity = imageRepository.findByAccountAndImageType(userEntity, ImageType.ACCOUNT_IMAGE);
        if (imageEntity != null) {
            userDTO.setImageUrl(imageEntity.getImageUrl());
        }
        return userDTO;
    }

    private UserDTO convertToDTO(UserEntity userEntity) {
        return modelMapper.map(userEntity, UserDTO.class);
    }


    private ImageEntity createImageEntity(UserEntity userEntity, File destinationFile, UserEntity currentUser) {
        return ImageEntity.builder()
                .imageUrl(destinationFile.getPath())
                .imageType(ImageType.ACCOUNT_IMAGE)
                .account(currentUser)
                .account(userEntity)
                .build();
    }


}
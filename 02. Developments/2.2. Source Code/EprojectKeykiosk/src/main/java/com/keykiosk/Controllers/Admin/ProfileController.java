package com.keykiosk.Controllers.Admin;

import com.keykiosk.Exception.RegistrationException;
import com.keykiosk.Models.DTO.CustomerDTO;
import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.GenderType;
import com.keykiosk.Models.Model;
import com.keykiosk.Observer.UserObservable;
import com.keykiosk.Services.CustomerService;
import com.keykiosk.Services.UserService;
import com.keykiosk.Util.AlertUtil;
import com.keykiosk.Util.FileUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class ProfileController implements Initializable {

    @FXML
    private Label dataOfJoining_lbl, email_lbl, fullname_lbl, role_lbl, username_lbl;

    @FXML
    private ImageView profileImageView;

    @FXML
    private TextField confirmPasswordTextField, currentPasswordTextField, newPasswordTextField, fullNameTextField, phoneTextField, addressTextField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private Button updatePasswordButton, changeImagesButton, saveCustomerButton;

    @Autowired
    private UserService userService;

    @Autowired
    private UserObservable userObservable;

    @Autowired
    private CustomerService customerService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bindUserData();
        updatePasswordButton.setOnAction(event -> handleChangePassword());
        changeImagesButton.setOnAction(event -> handleChangeImage());
        saveCustomerButton.setOnAction(event -> saveCustomer());

        loadAndSetUserImage();
        loadAllGenderToComboBox();
        loadCustomerData();
    }

    private void bindUserData() {
        Model model = Model.getInstance();
        fullname_lbl.textProperty().bind(model.fullNameProperty());
        username_lbl.textProperty().bind(model.usernameProperty());
        email_lbl.textProperty().bind(model.emailProperty());
        role_lbl.textProperty().bind(model.roleProperty());

        StringBinding formattedDateBinding = Bindings.createStringBinding(() ->
                formatDateTime(model.createdAtProperty().get()), model.createdAtProperty());
        dataOfJoining_lbl.textProperty().bind(formattedDateBinding);
    }

    private void loadCustomerData() {
        CustomerDTO customerDTO = customerService.findByEmail(Model.getInstance().getLoggedInUser().getEmail());
        if (customerDTO != null) {
            fullNameTextField.setText(customerDTO.getFullName());
            phoneTextField.setText(customerDTO.getPhoneNumber());
            addressTextField.setText(customerDTO.getAddress());
            genderComboBox.setValue(customerDTO.getGender().name());
        }
    }

    private void loadAndSetUserImage() {
        userService.getImageUrlForUser().stream()
                .filter(userDTO -> userDTO.getImageUrl() != null)
                .map(userDTO -> new Image("file:" + userDTO.getImageUrl()))
                .findFirst()
                .ifPresent(profileImageView::setImage);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
    }

    private void handleChangePassword() {
        UserEntity user = Model.getInstance().getLoggedInUser();
        String currentPassword = currentPasswordTextField.getText();
        String newPassword = newPasswordTextField.getText();
        String confirmPassword = confirmPasswordTextField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
            return;
        }

        try {
            if (userService.login(user.getUsername(), currentPassword).isPresent()) {
                userService.changePassword(user, currentPassword, newPassword);
                clearPasswordFields();
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully");
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", "Current password is incorrect");
            }
        } catch (Exception e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void handleChangeImage() {
        File selectedFile = FileUtil.selectImage(profileImageView);
        if (selectedFile != null) {
            try {
                userService.insertImageProfile(selectedFile);
                userObservable.notifyUser();
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Image changed successfully");
            } catch (Exception e) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    private void saveCustomer() {
        String fullName = fullNameTextField.getText();
        String email = Model.getInstance().getLoggedInUser().getEmail();
        UserEntity userEntity = userService.findByEmail(email);

        if (userEntity == null) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", "Logged-in user not found");
            return;
        }

        try {
            userService.updateFullName(userEntity.getAccountId(), fullName);
            Model.getInstance().setLoggedInUser(userEntity);
            Model.getInstance().fullNameProperty().set(fullName);

            CustomerDTO customerDTO = CustomerDTO.builder()
                    .fullName(fullName)
                    .email(email)
                    .gender(GenderType.valueOf(genderComboBox.getValue()))
                    .phoneNumber(phoneTextField.getText())
                    .address(addressTextField.getText())
                    .build();

            if (customerService.findByEmail(email) != null) {
                customerService.updateCustomer(customerDTO);
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Customer information updated successfully");
            } else {
                customerService.createCustomer(customerDTO);
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Customer information saved successfully");
            }
        } catch (RegistrationException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadAllGenderToComboBox() {
        for (GenderType genderType : GenderType.values()) {
            genderComboBox.getItems().add(genderType.name());
        }
    }

    private void clearPasswordFields() {
        currentPasswordTextField.clear();
        newPasswordTextField.clear();
        confirmPasswordTextField.clear();
    }
}

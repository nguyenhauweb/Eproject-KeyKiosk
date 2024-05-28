package com.keykiosk.Controllers.Admin;

import com.jfoenix.controls.JFXButton;
import com.keykiosk.Exception.RegistrationException;
import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.EnumType.Role;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Observer.UserObservable;
import com.keykiosk.Services.UserService;
import com.keykiosk.Validators.UserValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import static com.keykiosk.Util.AlertUtil.showAlert;

@Component
public class UpdateAccountController implements Initializable {
    @FXML
    private JFXButton CloseButton, UpdateButton;
    @FXML
    private TextField EmailField, FullNameField, UsernameField;
    @FXML
    private PasswordField PasswordField;
    @FXML
    private ComboBox<String>  statusComboBox;
    @Setter
    private Stage stage;
    @Autowired
    private UserService userService;
    private UserDTO userToUpdate;
    @Setter
    private Runnable afterUpdate;
    private String oldPassword;

    @Autowired
    private UserObservable userObservable;

    public void setUserDTO(UserDTO userDTO) {
        this.userToUpdate = userDTO;
        FullNameField.setText(userDTO.getFullName());
        EmailField.setText(userDTO.getEmail());
        UsernameField.setText(userDTO.getUsername());
        statusComboBox.setValue(userDTO.getStatus().toString());
        oldPassword = userDTO.getPasswordHash();
    }

    private void updateUser(ActionEvent event) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        String email = EmailField.getText();
        String username = UsernameField.getText();
        String password = PasswordField.getText();
        String fullName = FullNameField.getText();

        if (UserValidator.validateEmail(email) || UserValidator.validateUsername(username) || UserValidator.validateFullName(fullName)) {
            return;
        }

        if (password.isEmpty()) {
            password = oldPassword;
        } else {
            if (UserValidator.validatePassword(password)) {
                return;
            }
        }

        if (statusComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a status");
            return;
        }
        Status status = Status.valueOf(statusComboBox.getValue());

//        UserDTO updatedUser = new UserDTO(userToUpdate.getId(), email, username, password, fullName, userToUpdate.getBalance(), Role.SELLER, status);
        UserDTO updatedUser = UserDTO.builder()
                .id(userToUpdate.getId())
                .email(email)
                .username(username)
                .passwordHash(password)
                .fullName(fullName)
                .balance(userToUpdate.getBalance())
                .role(Role.SELLER)
                .status(status)
                .build();
        if (!password.equals(oldPassword)) {
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(updatedUser);

            if (!violations.isEmpty()) {
                String errorMessage = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("\n"));
                showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage);
                return;
            }
        }

        try {
            updatedUser.setPasswordHash(new BCryptPasswordEncoder(12).encode(updatedUser.getPasswordHash()));
            userService.updateUser(updatedUser);
            userObservable.notifyUser();
            clearFields();
            afterUpdate.run();
            Model.getInstance().getViewFactory().closeStage(stage);
        } catch (RegistrationException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void clearFields() {
        FullNameField.clear();
        EmailField.clear();
        UsernameField.clear();
        PasswordField.clear();
        statusComboBox.getSelectionModel().clearSelection();
    }

    private void createComboBoxes() {
        statusComboBox.getItems().addAll(Status.ACTIVE.name(), Status.INACTIVE.name());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UpdateButton.setOnAction(this::updateUser);
        CloseButton.setOnAction(event -> Model.getInstance().getViewFactory().closeStage(stage));
        createComboBoxes();
    }
}
package com.keykiosk.Controllers.Authentication;

import com.keykiosk.Exception.RegistrationException;
import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.EnumType.Role;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Services.UserService;
import com.keykiosk.Util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class RegisterController implements Initializable {

    public Button verify_btn;
    public TextField verificationCodeField;
    @Autowired
    private UserService userService;

    @FXML
    private PasswordField cPassword_fld;
    @FXML
    private TextField email_fld;

    @FXML
    private TextField fullName_fld;

    @FXML
    private Button login_btn;

    @FXML
    private PasswordField password_fld;

    @FXML
    private Button register_btn;

    @FXML
    private TextField username_fld;


    private void clearFields() {
        username_fld.clear();
        email_fld.clear();
        password_fld.clear();
        fullName_fld.clear();
        cPassword_fld.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        register_btn.setOnAction(this::handleRegisterButton);
        verify_btn.setOnAction(this::handleVerifyButton);
        login_btn.setOnAction(this::handleLoginButton);
    }

    private void handleLoginButton(ActionEvent actionEvent) {
        try {
            Model.getInstance().getViewFactory().showLoginWindow();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void handleRegisterButton(ActionEvent actionEvent) {
        String username = username_fld.getText();
        String email = email_fld.getText();
        String password = password_fld.getText();
        String cPassword = cPassword_fld.getText();
        String fullName = fullName_fld.getText();
         UserDTO.builder()
                .email(email)
                .username(username)
                .passwordHash(password)
                .fullName(fullName)
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();



        if (!password.equals(cPassword)) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", "Password and confirm password do not match.");
            return;
        }

        try {
            userService.addVerificationCodeAndSendEmail(email);
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Please check your email for verification code.");
            verificationCodeField.setVisible(true);
            verificationCodeField.setDisable(false);
            register_btn.setDisable(true);
            verify_btn.setVisible(true);
        } catch (RegistrationException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void handleVerifyButton(ActionEvent actionEvent) {
        String verificationCode = verificationCodeField.getText();
        String email = email_fld.getText();

        try {
            if (userService.verifyConfirmationCode(email, verificationCode)) {
                UserDTO userDTO = UserDTO.builder()
                        .email(email)
                        .username(username_fld.getText())
                        .passwordHash(password_fld.getText())
                        .fullName(fullName_fld.getText())
                        .role(Role.ADMIN)
                        .status(Status.ACTIVE)
                        .build();
                userService.addUser(userDTO);
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "User has been added successfully.");
                clearFields();
            } else {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", "Invalid verification code.");
            }
        } catch (RegistrationException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }


}

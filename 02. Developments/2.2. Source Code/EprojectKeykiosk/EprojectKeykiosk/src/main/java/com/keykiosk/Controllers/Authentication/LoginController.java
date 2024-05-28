package com.keykiosk.Controllers.Authentication;

import com.keykiosk.Controllers.Admin.AdminMenuController;
import com.keykiosk.Exception.RegistrationException;
import com.keykiosk.MainApp;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.Entity.UserPrincipal;
import com.keykiosk.Models.Model;
import com.keykiosk.Services.Impl.UserServiceImpl;
import com.keykiosk.Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.keykiosk.Models.EnumType.Role.*;
import static com.keykiosk.Util.AlertUtil.showAlert;

@Controller
public class LoginController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        login_btn.setOnAction(this::handleLoginButton);
        signUp_btn.setOnAction(this::handleRegisterButton);
//        resetPass_btn.setOnAction(this::handleResetPassBtn);
    }

    @FXML
    private Label error_lbl;

    @FXML
    private Button login_btn;

    @FXML
    private PasswordField password_fld;

    @FXML
    private Label password_lbl;

    @FXML
    private Button resetPass_btn;

    @FXML
    private Button signUp_btn;

    @FXML
    private TextField username_fld;

    @FXML
    private Label username_lbl;
    @Autowired
    private UserService accountService;


    private void handleLoginButton(ActionEvent actionEvent) {
        try {
            String username = username_fld.getText();
            String password = password_fld.getText();
            // Check if the account is locked
            if (UserServiceImpl.isAccountLocked()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Your account is locked. Please contact the administrator to unlock your account.");
                return;
            }
            // Perform login
            Optional<UserEntity> optionalUser = accountService.login(username, password);
            UserEntity userEntity = optionalUser.get();
            // Set the authentication
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    new UserPrincipal(userEntity),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(userEntity.getRole().name()))
            );
            // Set the authentication
            SecurityContextHolder.getContext().setAuthentication(auth);
            // Update the authentication
            AdminMenuController adminMenuController = MainApp.getApplicationContext().getBean(AdminMenuController.class);
            adminMenuController.updateAuthentication();
            // Show the account window
            handleSuccessfulLogin(optionalUser.get());
            boolean isLoggedIn = Model.getInstance().isLoggedIn();
            System.out.println("Is user logged in? " + isLoggedIn);
        } catch (RegistrationException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void handleRegisterButton(ActionEvent actionEvent) {
        try {
            Model.getInstance().getViewFactory().showRegisterWindow();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void handleSuccessfulLogin(UserEntity account) {
        Model.getInstance().setLoggedInUser(account);
        if (account.getRole().equals(USER)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful User!");
//            Model.getInstance().getViewFactory().showClientWindow();
        } else if (account.getRole().equals(ADMIN)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful Admin!");
            Model.getInstance().getViewFactory().showAdminWindow();
        } else if (account.getRole().equals(SELLER)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful Seller!");
            Model.getInstance().getViewFactory().showAdminWindow();
        }

    }


}

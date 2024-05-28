package com.keykiosk.Validators;

import javafx.scene.control.Alert;

import static com.keykiosk.Util.AlertUtil.showAlert;

public class UserValidator {

    public static boolean validateEmail(String email) {
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Email is required");
            return true;
        }
        return false;
    }

    public static boolean validateUsername(String username) {
        if (username.length() < 3 || username.length() > 50) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username must be between 3 and 50 characters");
            return true;
        }
        return false;
    }

    public static boolean validatePassword(String password) {
        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password must be at least 6 characters");
            return true;
        }
        return false;
    }

    public static boolean validateFullName(String fullName) {
        if (fullName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Full name is required");
            return true;
        }
        return false;
    }


}

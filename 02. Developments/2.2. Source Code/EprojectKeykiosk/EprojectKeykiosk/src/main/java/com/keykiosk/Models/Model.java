package com.keykiosk.Models;

import com.keykiosk.Controllers.Admin.AdminController;
import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Views.ViewFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.BorderPane;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class Model {
    private static Model model;
    private final ViewFactory viewFactory;
    private final UserEntity loggedInUser; // Tham chiếu đến tài khoản người dùng hiện đang đăng nhập
    private final ObjectProperty<UserEntity> loggedInUserProperty = new SimpleObjectProperty<>(); // Tham chiếu đến tài khoản người dùng hiện đang đăng nhập
    private final SimpleStringProperty fullName = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    private final SimpleStringProperty username = new SimpleStringProperty();
    private final SimpleStringProperty role = new SimpleStringProperty();
    private final SimpleObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private Model() {
        this.viewFactory = new ViewFactory();
        loggedInUser = null;
    }


    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }


    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static Model getModel() {
        return model;
    }
    public SimpleStringProperty fullNameProperty() {
        return fullName;
    }
    public static void setModel(Model model) {
        Model.model = model;
    }
    public void setLoggedInUser(UserEntity user) {
        loggedInUserProperty.set(user);
        if (user != null) {
            this.fullName.set(user.getFullName());
            this.email.set(user.getEmail());
            this.username.set(user.getUsername());
            this.role.set(user.getRole().toString());
            this.createdAt.set(user.getCreatedAt());
        }
    }


    public UserEntity getLoggedInUser() {
        return loggedInUserProperty.get();
    }
    public SimpleStringProperty emailProperty() {
        return email;
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public SimpleStringProperty roleProperty() {
        return role;
    }

    public SimpleObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }
//    public void setLoggedInUser(UserEntity user) {
//        loggedInUserProperty.set(user);
//    }
//    // Trong UserService.java
//    public void logout() {
//        // Xóa thông tin phiên làm việc của người dùng
//        SecurityContextHolder.clearContext();
//
//        // Đặt đối tượng người dùng đăng nhập là null
//        loggedInUser = null;
//        //debug logged in user
//        System.out.println("Logged in user after logout: " + loggedInUser);
//
//        // In ra thông tin xác thực sau khi đã gọi clearContext
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("Authentication after logout: " + auth);
//    }

    public void logout() {
        // Xóa thông tin phiên làm việc của người dùng
        SecurityContextHolder.clearContext();

        // Đặt đối tượng người dùng đăng nhập là null
        loggedInUserProperty.set(null);
        //debug logged in user
//        System.out.println("Logged in user after logout: " + getLoggedInUser());
//
//        // In ra thông tin xác thực sau khi đã gọi clearContext
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("Authentication after logout: " + auth);
    }



}

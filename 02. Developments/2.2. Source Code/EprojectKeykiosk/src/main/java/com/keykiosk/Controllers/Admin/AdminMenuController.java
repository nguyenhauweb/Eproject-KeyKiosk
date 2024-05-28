package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.PermissionDTO;
import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.EnumType.PermissionType;
import com.keykiosk.Models.EnumType.Role;
import com.keykiosk.Models.Model;
import com.keykiosk.Observer.PermissionObservable;
import com.keykiosk.Observer.PermissionObserver;
import com.keykiosk.Observer.UserObservable;
import com.keykiosk.Observer.UserObserver;
import com.keykiosk.Services.PermissionService;
import com.keykiosk.Views.AdminMenuOptions;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Services.UserService;

@Component
public class AdminMenuController implements Initializable, PermissionObserver, UserObserver {
    @FXML
    private VBox buttonContainer;
    @FXML
    private Button dashboard_btn, accounts_btn, profile_btn, logout_btn, category_btn, orders_btn, product_btn, softwareAccount_btn, softwareLicensee_btn;
    @FXML
    private ImageView avatarUserImageView;
    @FXML
    private Label roleUser_lbl;
    @FXML
    private Label fullNameUser_lbl;
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionObservable permissionObservable;
    @Autowired
    private PermissionService permissionService;

    private Button activeButton;

    private final BooleanProperty accountsVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty categoryVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty productTypeVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty productVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty softwareAccountVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty softwareLicenseeVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty ordersVisible = new SimpleBooleanProperty(true);

    private final List<Button> originalOrder = new ArrayList<>();
    @Autowired
    private UserObservable userObservable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accounts_btn.visibleProperty().bind(accountsVisible);
        category_btn.visibleProperty().bind(categoryVisible);
        product_btn.visibleProperty().bind(productVisible);
        softwareAccount_btn.visibleProperty().bind(softwareAccountVisible);
        softwareLicensee_btn.visibleProperty().bind(softwareLicenseeVisible);
        orders_btn.visibleProperty().bind(ordersVisible);

        updateAuthentication();
        permissionObservable.addObserver(this);
        originalOrder.addAll(Arrays.asList(dashboard_btn, accounts_btn, category_btn, product_btn, softwareAccount_btn, softwareLicensee_btn, orders_btn, profile_btn, logout_btn));

        addListeners();
        dashboard_btn.fire();
        loadAndSetUserImage();
        userObservable.addObserver(this);
        loadUserData();
    }

    private void loadAndSetUserImage() {
        List<UserDTO> userDTOs = userService.getImageUrlForUser();
        for (UserDTO userDTO : userDTOs) {
            if (userDTO.getImageUrl() != null) {
                Image image = new Image("file:" + userDTO.getImageUrl());
                avatarUserImageView.setImage(image);
            }
        }
    }

    private void loadUserData() {
        Model model = Model.getInstance();
        UserEntity user = model.getLoggedInUser();
        if (user != null) {
            fullNameUser_lbl.textProperty().bind(model.fullNameProperty());
            roleUser_lbl.textProperty().bind(model.roleProperty());
        }
    }

    private void addListeners() {
        dashboard_btn.setOnAction(this::handleButtonAction);
        accounts_btn.setOnAction(this::handleButtonAction);
        logout_btn.setOnAction(this::handleLogoutButton);
        category_btn.setOnAction(this::handleButtonAction);
        orders_btn.setOnAction(this::handleButtonAction);
        product_btn.setOnAction(this::handleButtonAction);
        softwareAccount_btn.setOnAction(this::handleButtonAction);
        softwareLicensee_btn.setOnAction(this::handleButtonAction);
        profile_btn.setOnAction(this::handleButtonAction);
    }

    private void handleButtonAction(ActionEvent event) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("my-button-active");
        }

        activeButton = (Button) event.getSource();
        activeButton.getStyleClass().add("my-button-active");

        navigateToPage(activeButton.getId());
    }

    private void navigateToPage(String id) {
        switch (id) {
            case "dashboard_btn" -> Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(AdminMenuOptions.DASHBOARD);
            case "accounts_btn" -> Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(AdminMenuOptions.ACCOUNTS);
            case "category_btn" -> Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(AdminMenuOptions.CATEGORY);
            case "product_btn" -> Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(AdminMenuOptions.PRODUCTS);
            case "softwareAccount_btn" -> Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(AdminMenuOptions.SOFTWARE_ACCOUNTS);
            case "softwareLicensee_btn" -> Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(AdminMenuOptions.SOFTWARE_LICENSES);
            case "orders_btn" -> Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(AdminMenuOptions.ORDERS);
            case "profile_btn" -> Model.getInstance().getViewFactory().getClientSelectedMenuItem().set(AdminMenuOptions.PROFILE);
        }
    }

    public void updateAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserEntity currentUser = userService.findUserByUsername(authentication.getName());
            if (currentUser != null) {
                List<PermissionDTO> permissions = permissionService.getPermissionsByUser(currentUser);
                if (currentUser.getRole().equals(Role.ADMIN) && currentUser.getUsername().equals("admin")) {
                    setAllButtonsVisibility(true);
                } else {
                    setAllButtonsVisibility(false);
                }

                for (PermissionDTO permission : permissions) {
                    PermissionType permissionType = PermissionType.valueOf(permission.getPermissionType());
                    switch (permissionType) {
                        case ACCOUNT -> accountsVisible.set(true);
                        case CATEGORY -> categoryVisible.set(true);
                        case PRODUCT_TYPE -> productTypeVisible.set(true);
                        case PRODUCT -> productVisible.set(true);
                        case SOFTWARE_ACCOUNT -> softwareAccountVisible.set(true);
                        case SOFTWARE_LICENSE -> softwareLicenseeVisible.set(true);
                        case ORDER -> ordersVisible.set(true);
                    }
                }
                updateButtonVisibility();
            }
        } else {
            setAllButtonsVisibility(false);
        }
    }

    private void setAllButtonsVisibility(boolean visibility) {
        accountsVisible.set(visibility);
        categoryVisible.set(visibility);
        productTypeVisible.set(visibility);
        productVisible.set(visibility);
        softwareAccountVisible.set(visibility);
        softwareLicenseeVisible.set(visibility);
        ordersVisible.set(visibility);
    }

    private void updateButtonVisibility() {
        if (buttonContainer == null) return;
        buttonContainer.getChildren().removeIf(button -> !button.visibleProperty().get());
        for (Button button : originalOrder) {
            if (!buttonContainer.getChildren().contains(button) && button.visibleProperty().get()) {
                buttonContainer.getChildren().add(originalOrder.indexOf(button), button);
            }
        }
    }

    private void handleLogoutButton(ActionEvent actionEvent) {
        Model.getInstance().logout();
        ((Stage) logout_btn.getScene().getWindow()).close();
        Model.getInstance().getViewFactory().showLoginWindow();
        originalOrder.clear();
    }

    @Override
    public void updatePermissions() {
        Platform.runLater(this::updateAuthentication);
    }

    @Override
    public void update() {
        PermissionObserver.super.update();
    }

    @Override
    public void updateUser() {
        Platform.runLater(() -> {
            loadAndSetUserImage();
            loadUserData();
        });
    }
}

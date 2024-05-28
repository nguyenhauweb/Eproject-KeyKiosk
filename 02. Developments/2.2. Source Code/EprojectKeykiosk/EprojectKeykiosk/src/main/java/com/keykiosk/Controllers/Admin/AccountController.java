package com.keykiosk.Controllers.Admin;

import com.jfoenix.controls.JFXButton;
import com.keykiosk.Exception.RegistrationException;
import com.keykiosk.Models.DTO.PermissionDTO;
import com.keykiosk.Models.DTO.UserDTO;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.PermissionType;
import com.keykiosk.Models.EnumType.Role;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Services.PermissionService;
import com.keykiosk.Services.UserService;
import com.keykiosk.Validators.UserValidator;
import com.keykiosk.Views.ViewFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.keykiosk.Util.AlertUtil.showAlert;
import static com.keykiosk.Util.AlertUtil.showConfirmDialog;

@Component
public class AccountController implements Initializable {
    public JFXButton permissionButton;
    public JFXButton addNewButton;
    public ComboBox<String> searchCombobox;
    @FXML
    private TextField emailTextField;

    @FXML
    private TextField fullNameTextField;

    @FXML
    private TextField passwordHashTextField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TableView<UserDTO> userTableView;
    @FXML
    private TextField searchField;

    @FXML
    private TextField usernameTextField;
    private ObservableList<UserDTO> users;

    @Autowired
    private ViewFactory viewFactory;

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;
    private Map<PermissionType, CheckBox> permissionCheckboxMap;
    private final ObservableList<PermissionDTO> permissionDTOs = FXCollections.observableArrayList();
    private UserDTO user;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        users = FXCollections.observableArrayList();
        userTableView.setItems(users);
        // Set the items for the ComboBoxes
        roleComboBox.setItems(FXCollections.observableArrayList(Role.SELLER.name()));
//        statusComboBox.setItems(FXCollections.observableArrayList(Status.ACTIVE.name(), Status.INACTIVE.name()));
        searchCombobox.setItems(FXCollections.observableArrayList("Everything", "Email", "Username", "Full Name"));
        searchCombobox.setValue("Everything");
        initializeTableView();
        loadUsers();

        addNewButton.setOnAction(this::handleCreateUser);

        permissionButton.setOnAction(this::handlePermissionButtonAction);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchType = searchCombobox.getValue();

            CompletableFuture.supplyAsync(() -> {
                List<UserDTO> result;
                switch (searchType) {
                    case "Email" -> result = userService.searchUsersByEmail(newValue);
                    case "Username" -> result = userService.searchUsersByUsername(newValue);
                    case "Full Name" -> result = userService.searchUsersByFullName(newValue);
                    default -> result = userService.searchUsers(newValue);
                }
                return result;
            }).thenAccept(result -> {
                Platform.runLater(() -> {
                    users.clear();
                    users.addAll(result);
                });
            });
        });
    }

    private void loadUsers() {
        CompletableFuture.supplyAsync(() -> userService.getAllUsers())
                .thenAcceptAsync(result -> {
                    Platform.runLater(() -> {
                        users.clear();
                        List<UserDTO> filteredUsers = result.stream()
                                .filter(user -> !user.getRole().equals(Role.ADMIN)) // Ignore users with the role 'ADMIN'
                                .collect(Collectors.toList());
                        users.addAll(filteredUsers);
                    });
                });
    }
    // Khởi tạo TableView
    private void initializeTableView() {
        TableColumn<UserDTO, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setResizable(false);

        TableColumn<UserDTO, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<UserDTO, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setResizable(false);

        TableColumn<UserDTO, String> fullNameColumn = new TableColumn<>("Full Name");
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<UserDTO, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setResizable(false);

        TableColumn<UserDTO, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setResizable(false);
        userTableView.getColumns().addAll(idColumn, emailColumn, usernameColumn, fullNameColumn, roleColumn, statusColumn);
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        TableColumn<UserDTO, Void> actionsColumn = viewFactory.createActionColumn(this::updateEmployee, this::deleteUser);
        actionsColumn.setResizable(false);
        userTableView.getColumns().add(actionsColumn);
    }

    @FXML
    private void deleteUser(UserDTO userDTO) {
        if (userDTO.getUsername().equals("admin")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete the admin user");
            return;
        }

        Optional<Boolean> result = showConfirmDialog("Confirm Delete", "Are you sure you want to delete this user?");
        if (result.isPresent() && result.get()) {
            userService.deleteUser(userDTO);
            users.remove(userDTO);
        }
    }

    private void updateEmployee(UserDTO userDTO) {
        if (userDTO.getUsername().equals("admin")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot update the admin user");
            return;
        }
        Platform.runLater(() -> {
            Model.getInstance().getViewFactory().showUpdateDialog(userDTO, this::loadUsers);
        });
    }

    @FXML
    private void handleCreateUser(ActionEvent event) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        String email = emailTextField.getText();
        String username = usernameTextField.getText();
        String password = passwordHashTextField.getText();
        String fullName = fullNameTextField.getText();

        if (UserValidator.validateEmail(email) || UserValidator.validateUsername(username) || UserValidator.validatePassword(password) || UserValidator.validateFullName(fullName)) {
            return;
        }

        if (roleComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a role");
            return;
        }
        Role role = UserDTO.getRoleFromComboBox(roleComboBox.getValue());

//        if (statusComboBox.getValue() == null) {
//            showAlert(Alert.AlertType.ERROR, "Error", "Please select a status");
//            return;
//        }
//        Status status = UserDTO.getStatusFromComboBox(statusComboBox.getValue());

//        UserDTO newUser = new UserDTO(null, email, username, password, fullName, BigDecimal.ZERO, role, Status.ACTIVE);
        UserDTO newUser = UserDTO.builder()
                .id(null)
                .email(email)
                .username(username)
                .passwordHash(password)
                .fullName(fullName)
                .balance(BigDecimal.ZERO)
                .role(role)
                .status(Status.ACTIVE)
                .build();
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(newUser);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("\n"));
            showAlert(Alert.AlertType.ERROR, "Validation Error", errorMessage);
        } else {
            try {
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                newUser.setPasswordHash(passwordEncoder.encode(password));
                userTableView.getItems().add(0, userService.createUser(newUser));
                clearFields();
            } catch (RegistrationException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    private void clearFields() {
        emailTextField.clear();
        usernameTextField.clear();
        passwordHashTextField.clear();
        fullNameTextField.clear();
        roleComboBox.getSelectionModel().clearSelection();
//        statusComboBox.getSelectionModel().clearSelection();
    }

    public List<UserDTO> searchUsers(String keyword) {
        return userService.searchUsers(keyword);
    }

    @FXML
    private void handlePermissionButtonAction(ActionEvent event) {
        UserDTO selectedUser = userTableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No User Selected", "Please select a user from the table.");
            return;
        }

        user = selectedUser;

        CheckBox accountCheckbox = new CheckBox("Account");
        CheckBox categoryCheckbox = new CheckBox("Category");
        CheckBox productTypeCheckbox = new CheckBox("Product Type");
        CheckBox productCheckbox = new CheckBox("Product");
        CheckBox softwareAccountCheckbox = new CheckBox("Software Account");
        CheckBox LicenseKeyCheckbox = new CheckBox("License Key");
        CheckBox OrderCheckbox = new CheckBox("Order");


        permissionCheckboxMap = Map.of(
                PermissionType.ACCOUNT, accountCheckbox,
                PermissionType.CATEGORY, categoryCheckbox,
                PermissionType.PRODUCT_TYPE, productTypeCheckbox,
                PermissionType.PRODUCT, productCheckbox,
                PermissionType.SOFTWARE_ACCOUNT, softwareAccountCheckbox,
                PermissionType.SOFTWARE_LICENSE, LicenseKeyCheckbox,
                PermissionType.ORDER, OrderCheckbox

        );

        initPermissionCheckboxes();
        bindCheckboxes();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Update Permissions");
        alert.setHeaderText("Update permissions for " + user.getUsername());

        VBox checkboxContainer = new VBox();
        checkboxContainer.getChildren().addAll(accountCheckbox, categoryCheckbox, productCheckbox, productTypeCheckbox, softwareAccountCheckbox, LicenseKeyCheckbox, OrderCheckbox);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(checkboxContainer);
        alert.showAndWait();

        handleCheckboxAction(event);
    }


    private void initPermissionCheckboxes() {
        permissionDTOs.clear();

        List<PermissionDTO> permissions = permissionService.getPermissionsByUser(userService.getUserEntityById(user.getId()));
        if (permissions != null) {
            permissionDTOs.addAll(permissions);
        } else {
            System.out.println("Permissions not found for user: " + user);
        }
    }

    private void bindCheckboxes() {
        UserEntity userEntity = userService.getUserEntityById(user.getId());
        permissionCheckboxMap.forEach((type, checkbox) -> {
            boolean exists = permissionService.permissionExists(new PermissionDTO(null, type.toString(), true), userEntity);
            checkbox.setSelected(exists);
        });
    }


    @FXML
    private void handleCheckboxAction(ActionEvent event) {
        List<PermissionDTO> updatedPermissions = createPermissionDTOs();
        UserEntity userEntity = userService.getUserEntityById(user.getId());

        List<PermissionDTO> newPermissions = updatedPermissions.stream()
                .filter(permissionDTO -> !permissionService.permissionExists(permissionDTO, userEntity))
                .collect(Collectors.toList());

        permissionService.savePermissions(userEntity, newPermissions);
        removeUnselectedPermissions(userEntity);
    }

    private List<PermissionDTO> createPermissionDTOs() {
        return permissionCheckboxMap.entrySet().stream()
                .map(entry -> new PermissionDTO(
                        getPermissionDTO(entry.getKey(), permissionDTOs)
                                .map(PermissionDTO::getPermissionId)
                                .orElse(null),
                        entry.getKey().toString(),
                        entry.getValue().isSelected()
                ))
                .collect(Collectors.toList());
    }

    private void removeUnselectedPermissions(UserEntity userEntity) {
        permissionCheckboxMap.entrySet().stream()
                .filter(entry -> !entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .forEach(type -> permissionService.deleteByPermissionTypeAndAccount(type, userEntity));
    }


    private Optional<PermissionDTO> getPermissionDTO(PermissionType type, List<PermissionDTO> permissions) {
        return permissions.stream()
                .filter(p -> p.getPermissionType().equals(type.toString()))
                .findFirst()
                .or(() -> Optional.of(new PermissionDTO(permissions.size() + 1L, type.toString(), true)));
    }


}

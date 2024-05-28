package com.keykiosk.Views;

import com.keykiosk.Controllers.Admin.*;
import com.keykiosk.Controllers.Components.ActionButtonsController;
import com.keykiosk.MainApp;
import com.keykiosk.Models.DTO.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

@Component
public class ViewFactory {
    @Setter
    private Stage currentStage;
    private AnchorPane transactionsView;
    @Getter
    private final ObjectProperty<AdminMenuOptions> clientSelectedMenuItem;
    private AnchorPane accountsView;
    private AnchorPane dashboardView;
    private AnchorPane categoryView;
    private AnchorPane productTypeView;
    private AnchorPane productView;
    private AnchorPane softwareAccountView;
    private AnchorPane softwareLicenseView;
    private AnchorPane orderView;
    private AnchorPane orderHistoryView;
    private AnchorPane profile2;

    public ViewFactory() {
        Platform.runLater(Stage::new);
        this.clientSelectedMenuItem = new SimpleObjectProperty<>();
    }

    public void showLoginWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Authentication/Login.fxml"));
        loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
        createStage(loader);

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/ImageView.fxml"));
//        loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
//        createStage(loader);
    }

    public AnchorPane getDashboardView() {
        if (dashboardView == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Fxml/Admin/DashBoard.fxml"));
                fxmlLoader.setControllerFactory(MainApp.getApplicationContext()::getBean);
                dashboardView = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dashboardView;
    }

    public AnchorPane getCategoryView() {
        if (categoryView == null) {
            categoryView = loadFXMLView("/Fxml/Admin/Category");
        }
        return categoryView;
    }

    public AnchorPane getProductTypeView() {
        if (productTypeView == null) {
            productTypeView = loadFXMLView("/Fxml/Admin/ProductType2");
        }
        return productTypeView;
    }

    public AnchorPane getProductView() {
        if (productView == null) {
            productView = loadFXMLView("/Fxml/Admin/Product");
        }
        return productView;
    }

    public AnchorPane getSoftwareAccountsView() {
        if (softwareAccountView == null) {
            softwareAccountView = loadFXMLView("/Fxml/Admin/SoftwareAccount");
        }
        return softwareAccountView;
    }

    public AnchorPane getSoftwareLicensesView() {
        if (softwareLicenseView == null) {
            softwareLicenseView = loadFXMLView("/Fxml/Admin/SoftwareLicenseKey");
        }
        return softwareLicenseView;
    }

    public AnchorPane getProfileView() {
        if (orderHistoryView == null) {
            orderHistoryView = loadFXMLView("/Fxml/Admin/ProfileDialog");
        }
        return orderHistoryView;
    }

    public AnchorPane getOrdersView() {
        if (orderView == null) {
            orderView = loadFXMLView("/Fxml/Admin/Order");
        }
        return orderView;
    }

    public void showUpdateDialog(UserDTO userToUpdate, Runnable afterUpdate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/UpdateAccount.fxml"));
            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            Parent root = loader.load();
            UpdateAccountController controller = loader.getController();
            controller.setUserDTO(userToUpdate);
            controller.setAfterUpdate(afterUpdate);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
//            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/Images/logo.jpg"))));
            stage.setScene(scene);
            stage.setTitle("Update User");
            stage.setResizable(false);
            stage.show();
            controller.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createStage(FXMLLoader loader) {
        try {
            Parent root = loader.load();
            currentStage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/Images/logo.jpg"))));
        currentStage.setResizable(false);
        currentStage.setTitle("KeyKiosK");
        currentStage.centerOnScreen();
        currentStage.show();
    }

    public AnchorPane getTransactionsView() {
        if (transactionsView == null) {
            transactionsView = loadFXMLView("/Fxml/Client/Transactions");
        }
        return transactionsView;
    }

    public AnchorPane getAccountsView() {
        if (accountsView == null) {
            accountsView = loadFXMLView("/Fxml/Admin/Account");
        }
        return accountsView;
    }

    public AnchorPane loadFXMLView(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath + ".fxml"));
            fxmlLoader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML view: " + fxmlPath, e);
        }
    }

    public void showAdminWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/Admin.fxml"));
        loader.setControllerFactory(MainApp.getApplicationContext()::getBean);

        AdminController clientController = new AdminController();
        loader.setController(clientController);
        createStage(loader);
    }


    public void closeStage(Stage stage) {
        stage.close();
    }

    public void showRegisterWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Authentication/Register.fxml"));
        loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
        createStage(loader);
    }


    public TableColumn<UserDTO, Void> createActionColumn(Consumer<UserDTO> onUpdate, Consumer<UserDTO> onDelete) {
        TableColumn<UserDTO, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final HBox buttonLayout;

            {
                Button deleteButton;
                Button updateButton;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Action/ActionButtons.fxml"));
                    buttonLayout = loader.load();
                    ActionButtonsController buttonsController = loader.getController();
                    updateButton = buttonsController.getUpdateButton();
                    deleteButton = buttonsController.getDeleteButton();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                updateButton.setOnAction(event -> onUpdate.accept(getTableRow().getItem()));
                deleteButton.setOnAction(event -> onDelete.accept(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonLayout);
            }
        });

        return actionsColumn;
    }

    public TableColumn<CategoryDTO, Void> createActionColumnCategory(Consumer<CategoryDTO> onUpdate, Consumer<CategoryDTO> onDelete) {
        TableColumn<CategoryDTO, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final HBox buttonLayout;

            {
                Button deleteButton;
                Button updateButton;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Action/ActionButtons.fxml"));
                    buttonLayout = loader.load();
                    ActionButtonsController buttonsController = loader.getController();
                    updateButton = buttonsController.getUpdateButton();
                    deleteButton = buttonsController.getDeleteButton();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                updateButton.setOnAction(event -> onUpdate.accept(getTableRow().getItem()));
                deleteButton.setOnAction(event -> onDelete.accept(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonLayout);
            }
        });

        return actionsColumn;
    }


    public TableColumn<ProductDTO, Void> createActionColumnProduct(Consumer<ProductDTO> onUpdate, Consumer<ProductDTO> onDelete) {
        TableColumn<ProductDTO, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final HBox buttonLayout;

            {
                Button deleteButton;
                Button updateButton;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Action/ActionButtons.fxml"));
                    buttonLayout = loader.load();
                    ActionButtonsController buttonsController = loader.getController();
                    updateButton = buttonsController.getUpdateButton();
                    deleteButton = buttonsController.getDeleteButton();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                updateButton.setOnAction(event -> onUpdate.accept(getTableRow().getItem()));
                deleteButton.setOnAction(event -> onDelete.accept(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonLayout);
            }
        });

        return actionsColumn;
    }

    public TableColumn<SoftwareAccountDTO, Void> createActionColumnSoftwareAccount(Consumer<SoftwareAccountDTO> onUpdate, Consumer<SoftwareAccountDTO> onDelete) {
        TableColumn<SoftwareAccountDTO, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final HBox buttonLayout;

            {
                Button deleteButton;
                Button updateButton;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Action/ActionButtons.fxml"));
                    buttonLayout = loader.load();
                    ActionButtonsController buttonsController = loader.getController();
                    updateButton = buttonsController.getUpdateButton();
                    deleteButton = buttonsController.getDeleteButton();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                updateButton.setOnAction(event -> onUpdate.accept(getTableRow().getItem()));
                deleteButton.setOnAction(event -> onDelete.accept(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonLayout);
            }
        });

        return actionsColumn;
    }

    public TableColumn<SoftwareLicenseKeyDTO, Void> createActionColumnSoftwareLicenseKey(Consumer<SoftwareLicenseKeyDTO> onUpdate, Consumer<SoftwareLicenseKeyDTO> onDelete) {
        TableColumn<SoftwareLicenseKeyDTO, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final HBox buttonLayout;

            {
                Button deleteButton;
                Button updateButton;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Action/ActionButtons.fxml"));
                    buttonLayout = loader.load();
                    ActionButtonsController buttonsController = loader.getController();
                    updateButton = buttonsController.getUpdateButton();
                    deleteButton = buttonsController.getDeleteButton();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                updateButton.setOnAction(event -> onUpdate.accept(getTableRow().getItem()));
                deleteButton.setOnAction(event -> onDelete.accept(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonLayout);
            }
        });

        return actionsColumn;
    }


    public void showUpdateDialogCategory(CategoryDTO imageCategoryDTO, Runnable afterUpdate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/UpdateCategory.fxml"));
            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            Parent root = loader.load();
            UpdateCategoryController controller = loader.getController();
            controller.setCategoryDTO(imageCategoryDTO);
            controller.setAfterUpdate(afterUpdate);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
//            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/Images/logo.jpg"))));
            stage.setScene(scene);
            stage.setTitle("Update Category");
            stage.setResizable(false);
            stage.show();
            controller.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showUpdateDialogSoftwareLicenseKey(SoftwareLicenseKeyDTO softwareLicenseKeyDTO, Runnable afterUpdate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/UpdateDialogSoftwareLicenseKey.fxml"));
            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            Parent root = loader.load();
            UpdateDialogSoftwareLicenseKey controller = loader.getController();
            controller.setSoftwareLicenseKeyDTO(softwareLicenseKeyDTO);
            controller.setAfterUpdate(afterUpdate);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
//            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/Images/logo.jpg"))));
            stage.setScene(scene);
            stage.setTitle("Update User");
            stage.setResizable(false);
            stage.show();
            controller.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUpdateDialogsoftwareAccount(SoftwareAccountDTO softwareAccountDTO, Runnable afterUpdate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/UpdateDialogsoftwareAccount.fxml"));
            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            Parent root = loader.load();
            UpdateDialogsoftwareAccount controller = loader.getController();
            controller.setUserDTO(softwareAccountDTO);
            controller.setAfterUpdate(afterUpdate);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
//            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/Images/logo.jpg"))));
            stage.setScene(scene);
            stage.setTitle("Update User");
            stage.setResizable(false);
            stage.show();
            controller.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showProfile2Window() {
        if (profile2 == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Fxml/Admin/UpdateProfile.fxml"));
                fxmlLoader.setControllerFactory(MainApp.getApplicationContext()::getBean);
                profile2 = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currentStage.setScene(new Scene(profile2));
        currentStage.setResizable(false);
        currentStage.setTitle("KeyKiosK");
        currentStage.centerOnScreen();
        currentStage.show();

    }

    public void showProfileWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/ProfileDialog.fxml"));
        loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
        createStage(loader);
    }

    public void showUpdateProductView(ProductDTO productDTO, Runnable afterUpdate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/UpdateProduct.fxml"));
            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            Parent root = loader.load();
            UpdateProductController controller = loader.getController();
            controller.setProductDTO(productDTO);
            controller.setAfterUpdate(afterUpdate);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
//            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/Images/logo.jpg"))));
            stage.setScene(scene);
            stage.setTitle("Update Product");
            stage.setResizable(false);
            stage.show();
            controller.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


//    public void showUpdateProductView(ProductDTO productDTO, Runnable afterUpdate) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/UpdateProduct.fxml"));
//            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
//            Parent root = loader.load();
//            UpdateProductController controller = loader.getController();
//            controller.setProductDTO(productDTO);
//            controller.setAfterUpdate(afterUpdate);
//            Scene scene = new Scene(root);
//            Stage stage = new Stage();
//            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.initOwner(currentStage);
////            stage.getIcons().add(new Image(String.valueOf(getClass().getResource("/Images/logo.jpg"))));
//            stage.setScene(scene);
//            stage.setTitle("Update Product");
//            stage.setResizable(false);
//            stage.show();
//            controller.setStage(stage);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    public void showAddNewProductView(Runnable afterUpdate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/CreateProduct.fxml"));
            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            Parent root = loader.load();
            CreateProductController controller = loader.getController();
            controller.setAfterUpdate(afterUpdate);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
            stage.setScene(scene);
            stage.setTitle("Add New Product");
            stage.setResizable(false);
            stage.show();
            controller.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showProductDetailView(ProductDTO productDTO) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/ProductDetails.fxml"));
            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            Parent root = loader.load();
            ProductDetailsController controller = loader.getController();
            controller.setProductDTO(productDTO);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
            stage.setScene(scene);
            stage.setTitle("Product Details");
            stage.setResizable(false);
            stage.show();
//            controller.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAddOrderView(ProductDTO productDTO ) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/AddOrder.fxml"));
            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
            Parent root = loader.load();
            AddOrderController controller = loader.getController();
            controller.setProductDTO(productDTO);
//            controller.setAfterUpdate(afterUpdate);
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
            stage.setScene(scene);
            stage.setTitle("Add Order");
            stage.setResizable(false);
            stage.show();
            controller.setStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/AddOrder.fxml"));
//            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
//            Parent root = loader.load();
//            AddOrderController controller = loader.getController();
//            controller.setProductDTO(productDTO);
//            Scene scene = new Scene(root);
//            Stage stage = new Stage();
//            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.initOwner(currentStage);
//            stage.setScene(scene);
//            stage.setTitle("Add Order");
//            stage.setResizable(false);
//            stage.show();
//            controller.setStage(stage);
//        } catch (IOException e) {
//            e.printStackTrace();
//    }
}

//    public void showUpdateDialogProfile(UserDTO userToUpdate, Runnable afterUpdate) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Admin/UpdateProfile.fxml"));
//            loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
//            Parent root = loader.load();
//            ProfileController controller = loader.getController(); // Change this line
//            controller.setUserDTO(userToUpdate);
//            controller.setAfterUpdate(afterUpdate);
//            Scene scene = new Scene(root);
//            Stage stage = new Stage();
//            stage.initModality(Modality.APPLICATION_MODAL);
//            stage.initOwner(currentStage);
//            stage.setScene(scene);
//            stage.setTitle("Update Profile");
//            stage.setResizable(false);
//            stage.show();
//            controller.setStage(stage);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
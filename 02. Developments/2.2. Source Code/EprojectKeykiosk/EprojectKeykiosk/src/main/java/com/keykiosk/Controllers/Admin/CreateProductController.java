package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.ProductStatus;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Models.Model;
import com.keykiosk.Observer.ProductObservable;
import com.keykiosk.Observer.ProductObserver;
import com.keykiosk.Services.CategoryService;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Util.FileUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.keykiosk.Util.AlertUtil.showAlert;

@Component
public class CreateProductController implements Initializable {

    @FXML
    private Button addnewButton, cancelButton, selectImageButton;
    @FXML
    private ComboBox<String> categoryComboBox, productTypeComboBox;
    @FXML
    private ImageView changeImages;
    @FXML
    private TextArea descriptionProductTextArea;
    @FXML
    private TextField nameProductField, priceProductField;

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @Setter
    private Runnable afterUpdate;
    @Setter
    private Stage stage;
    @Autowired
    private ProductObservable productObservable;

    private File selectedFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        loadAllCategoriesToComboBox();
        loadProductTypeCodesToComboBox();
    }

    private void setupEventHandlers() {
        addnewButton.setOnAction(event -> createProduct());
        cancelButton.setOnAction(event -> stage.close());
        selectImageButton.setOnAction(event -> selectImage());
    }

    private void createProduct() {
        try {
            ProductDTO productDTO = ProductDTO.builder()
                    .nameProduct(nameProductField.getText())
                    .productType(ProductTypeCode.valueOf(productTypeComboBox.getValue()))
                    .description(descriptionProductTextArea.getText())
                    .price(BigDecimal.valueOf(Double.parseDouble(priceProductField.getText())))
                    .quantity(0)
                    .status(ProductStatus.AVAILABLE)
                    .categoryId(categoryService.getCategoryByName(categoryComboBox.getValue()).getCategoryId())
                    .accountId(Model.getInstance().getLoggedInUser().getAccountId())
                    .build();
            productService.insertImageAndProduct(productDTO, selectedFile);
            productObservable.notifyProductObservers();
            afterUpdate.run();

            stage.close();
            clearFields();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create product");
        }
    }

    private void selectImage() {
        selectedFile = FileUtil.selectImage(changeImages);
    }

    private void loadAllCategoriesToComboBox() {
        CompletableFuture.supplyAsync(categoryService::getAllCategories)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    List<String> categoryNames = result.stream()
                            .map(CategoryDTO::getCategoryName)
                            .collect(Collectors.toList());
                    categoryComboBox.setItems(FXCollections.observableArrayList(categoryNames));
                }));
    }

    private void loadProductTypeCodesToComboBox() {
        for (ProductTypeCode productTypeCode : ProductTypeCode.values()) {
            productTypeComboBox.getItems().add(productTypeCode.name());
        }
    }

    private void clearFields() {
        nameProductField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        descriptionProductTextArea.clear();
        priceProductField.clear();
        productTypeComboBox.getSelectionModel().clearSelection();
        changeImages.setImage(null);
    }
}

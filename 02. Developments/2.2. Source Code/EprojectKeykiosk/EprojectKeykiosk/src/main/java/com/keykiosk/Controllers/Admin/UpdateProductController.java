package com.keykiosk.Controllers.Admin;

import com.jfoenix.controls.JFXButton;
import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.Entity.UserEntity;
import com.keykiosk.Models.EnumType.ProductStatus;
import com.keykiosk.Models.Model;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Util.FileUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

import static com.keykiosk.Util.AlertUtil.showAlert;

@Setter
@Component
public class UpdateProductController implements Initializable {
    private Stage stage;
    private ProductDTO productToUpdate;

    @FXML
    private JFXButton CloseButton;
    @FXML
    private JFXButton UpdateButton;
    @FXML
    private ImageView changeImages;
    @FXML
    private TextArea descriptionProductTextArea;
    @FXML
    private TextField priceProductField;
    @FXML
    private Button selectImageButton;
    @FXML
    private TextField productNameField;
    @FXML
    private ComboBox<String> statusComboBox;

    @Autowired
    private ProductService productService;

    private File selectedFile;

    @Setter
    private Runnable afterUpdate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UpdateButton.setOnAction(this::updateProduct);
        CloseButton.setOnAction(event -> Model.getInstance().getViewFactory().closeStage(stage));
        selectImageButton.setOnAction(e -> selectImage());
        loadProductStatusToComboBox();
    }

    public void setProductDTO(ProductDTO productDTO) {
        this.productToUpdate = productDTO;
        productNameField.setText(productDTO.getNameProduct());
        priceProductField.setText(productDTO.getPrice().toString());
        descriptionProductTextArea.setText(productDTO.getDescription());

        String imageUrl = productDTO.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            File imageFile = new File(imageUrl);
            if (imageFile.exists()) {
                changeImages.setImage(new Image(imageFile.toURI().toString()));
            }
        }
        statusComboBox.setValue(productDTO.getStatus().toString());
    }

    private void updateProduct(ActionEvent actionEvent) {
        productToUpdate.setNameProduct(productNameField.getText());
        productToUpdate.setProductType(productToUpdate.getProductType());
        productToUpdate.setDescription(descriptionProductTextArea.getText());
        productToUpdate.setPrice(BigDecimal.valueOf(Double.parseDouble(priceProductField.getText())));
        productToUpdate.setStatus(ProductStatus.valueOf(statusComboBox.getValue()));
        productToUpdate.setCategoryId(productToUpdate.getCategoryId());
        productToUpdate.setAccountId(Model.getInstance().getLoggedInUser().getAccountId());
        try {
            productService.updateImageAndProduct(productToUpdate, selectedFile);
            clearFields();
            if (afterUpdate != null) afterUpdate.run();
            Model.getInstance().getViewFactory().closeStage(stage);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product");
            e.printStackTrace();
        }
    }


    private void selectImage() {
        selectedFile = FileUtil.selectImage(changeImages);
    }


    private void loadProductStatusToComboBox() {
        for (ProductStatus status : ProductStatus.values()) {
            statusComboBox.getItems().add(status.name());
        }
    }

    private void clearFields() {
        productNameField.clear();
        descriptionProductTextArea.clear();
        priceProductField.clear();
        statusComboBox.getSelectionModel().clearSelection();
        changeImages.setImage(null);
        selectedFile = null;
    }
}

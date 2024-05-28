package com.keykiosk.Controllers.Admin;

import com.jfoenix.controls.JFXButton;
import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Services.CategoryService;
import com.keykiosk.Util.FileUtil;
import javafx.collections.FXCollections;
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
import java.net.URL;
import java.util.ResourceBundle;

import static com.keykiosk.Util.AlertUtil.showAlert;

@Component
public class UpdateCategoryController implements Initializable {

    @FXML
    private JFXButton CloseButton, UpdateButton;
    @FXML
    private TextField CategoryNameField;
    @FXML
    private TextArea DescriptionTextArea;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button selectImageButton;
    @FXML
    private ImageView changeImages;

    private File selectedFile;
    private CategoryDTO categoryToUpdate;

    @Setter
    private Stage stage;

    @Autowired
    private CategoryService categoryService;

    @Setter
    private Runnable afterUpdate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusComboBox.setItems(FXCollections.observableArrayList(Status.ACTIVE.name(), Status.INACTIVE.name()));
        UpdateButton.setOnAction(this::updateCategory);
        CloseButton.setOnAction(event -> Model.getInstance().getViewFactory().closeStage(stage));
        selectImageButton.setOnAction(e -> selectImage());
    }

    public void setCategoryDTO(CategoryDTO categoryDTO) {
        this.categoryToUpdate = categoryDTO;
        CategoryNameField.setText(categoryDTO.getCategoryName());
        DescriptionTextArea.setText(categoryDTO.getDescription());
        statusComboBox.setValue(categoryDTO.getStatus().toString());

        String imageUrl = categoryDTO.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            File imageFile = new File(imageUrl);
            if (imageFile.exists()) {
                changeImages.setImage(new Image(imageFile.toURI().toString()));
            }
        }
    }

    private void updateCategory(ActionEvent actionEvent) {
        categoryToUpdate.setCategoryId(categoryToUpdate.getCategoryId());
        categoryToUpdate.setImageId(categoryToUpdate.getImageId());
        categoryToUpdate.setCategoryName(CategoryNameField.getText());
        categoryToUpdate.setDescription(DescriptionTextArea.getText());
        categoryToUpdate.setStatus(Status.valueOf(statusComboBox.getValue()));
        try {
            categoryService.updateImageAndCategory(categoryToUpdate, selectedFile);
            clearFields();
            if (afterUpdate != null) afterUpdate.run();
            Model.getInstance().getViewFactory().closeStage(stage);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update category");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        CategoryNameField.clear();
        DescriptionTextArea.clear();
        statusComboBox.setValue(null);
        changeImages.setImage(null);
        selectedFile = null;
    }

    private void selectImage() {
        selectedFile = FileUtil.selectImage(changeImages);
    }
}

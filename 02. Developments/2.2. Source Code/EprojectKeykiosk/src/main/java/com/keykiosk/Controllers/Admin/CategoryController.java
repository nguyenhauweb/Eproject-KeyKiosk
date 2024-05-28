package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Observer.CategoryObservable;
import com.keykiosk.Services.CategoryService;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Util.AlertUtil;
import com.keykiosk.Views.ViewFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.keykiosk.Util.AlertUtil.showAlert;
import static com.keykiosk.Util.AlertUtil.showConfirmDialog;

@Component
public class CategoryController implements Initializable {
    @FXML
    private TextField categoryNameField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<CategoryDTO> categoryTableView;
    @FXML
    private ImageView changeImages;
    @FXML
    private Button selectImageButton;
    @FXML
    private Button addNewButton;
    @FXML
    private Button exportToExcelButton;
    @FXML
    private Button importFromExcelButton;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ViewFactory viewFactory;

    @Autowired
    private ProductService productService;

    private ObservableList<CategoryDTO> categoryList;
    private File selectedFile;

    @Autowired
    private CategoryObservable categoryObservable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryList = FXCollections.observableArrayList();
        categoryTableView.setItems(categoryList);
        initializeTableView();
        loadCategories();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        addNewButton.setOnAction(this::insertImageAndCategory);
        selectImageButton.setOnAction(e -> selectImage());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            categoryList.clear();
            categoryList.addAll(searchCategories(newValue));
        });
        exportToExcelButton.setOnAction(this::exportToExcelAction);
        importFromExcelButton.setOnAction(this::importFromExcelAction);
    }

    private void exportToExcelAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            String excelFilePath = selectedFile.getAbsolutePath();
            try {
                categoryService.exportDataToExcel(excelFilePath);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data exported successfully");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export data to Excel");
            }
        }
    }

    private void importFromExcelAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String excelFilePath = selectedFile.getAbsolutePath();
            try {
                categoryService.importDataFromExcel(excelFilePath);
                loadCategories();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data imported successfully");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to import data from Excel");
            }
        }
    }

    private List<CategoryDTO> searchCategories(String keyword) {
        return categoryService.searchCategories(keyword);
    }

    private void insertImageAndCategory(ActionEvent actionEvent) {
        String categoryName = categoryNameField.getText();
        String description = descriptionTextArea.getText();

        if (categoryName.isEmpty() || description.isEmpty() || selectedFile == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields and select an image");
            return;
        }
        if (categoryList.stream().anyMatch(category -> category.getCategoryName().equals(categoryName))) {
            showAlert(Alert.AlertType.ERROR, "Error", "Category name already exists");
            return;
        }

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryName(categoryName);
        categoryDTO.setDescription(description);
        categoryDTO.setStatus(Status.ACTIVE);

        try {
            categoryService.insertImageAndCategory(categoryDTO, selectedFile);
            loadCategories();
            clearFields();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to insert category");
        }
    }

    private void clearFields() {
        categoryNameField.clear();
        descriptionTextArea.clear();
        selectedFile = null;
        // Set the image view back to the default image
        Image defaultImage = new Image(getClass().getResource("/Images/upload.png").toExternalForm());
        changeImages.setImage(defaultImage);
    }

    private void loadCategories() {
        CompletableFuture.supplyAsync(categoryService::getAllImagesWithCategory)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    categoryList.clear();
                    categoryList.addAll(result);
                }));
    }

    private void initializeTableView() {
        TableColumn<CategoryDTO, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));

        TableColumn<CategoryDTO, ImageView> imageColumn = new TableColumn<>("Images");
        imageColumn.setCellValueFactory(data -> {
            String imageUrl = data.getValue().getImageUrl();
            File file = new File(imageUrl);
            ImageView imageView = (file.exists() && !file.isDirectory())
                    ? new ImageView(new Image("file:" + imageUrl))
                    : new ImageView(new Image("Images/upload.png"));
            imageView.setFitWidth(23);
            imageView.setFitHeight(23);
            return new SimpleObjectProperty<>(imageView);
        });

        TableColumn<CategoryDTO, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<CategoryDTO, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<CategoryDTO, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        categoryTableView.getColumns().addAll(idColumn, imageColumn, nameColumn, descriptionColumn, statusColumn);
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        TableColumn<CategoryDTO, Void> actionsColumn = viewFactory.createActionColumnCategory(this::updateCategory, this::deleteCategory);
        actionsColumn.setResizable(false);
        categoryTableView.getColumns().add(actionsColumn);
    }

    private void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            if (file.length() > 1 * 1024 * 1024) {
                showAlert(Alert.AlertType.ERROR, "Error", "The file is too large. Please select a file that is less than 1MB.");
                return;
            }
            selectedFile = file;
            Image image = new Image(selectedFile.toURI().toString());
            changeImages.setImage(image);
        }
    }

    private void updateCategory(CategoryDTO categoryDTO) {
        Platform.runLater(() -> viewFactory.showUpdateDialogCategory(categoryDTO, this::loadCategories));
    }


    private void deleteCategory(CategoryDTO categoryDTO) {
        // Check if any products are using this category
        List<ProductDTO> products = productService.findByCategoryId(categoryDTO.getCategoryId());
        if (!products.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete category because it is being used by one or more products.");
            return;
        }

        Optional<Boolean> result = AlertUtil.showConfirmDialog("Delete Product", "Are you sure you want to delete this product?");
        if (result.isPresent() && result.get()) {
            try {
                categoryService.deleteCategory(categoryDTO);
                categoryList.remove(categoryDTO);
                categoryObservable.notifyObservers();
            } catch (DataIntegrityViolationException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Cannot delete category because it is related to other records.");
            }
        }
    }
}

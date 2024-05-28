package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.CategoryDTO;
import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;
import com.keykiosk.Models.EnumType.ProductStatus;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Observer.*;
import com.keykiosk.Services.CategoryService;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Services.SoftwareAccountService;
import com.keykiosk.Services.SoftwareLicenseKeyService;
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
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.keykiosk.Util.AlertUtil.showAlert;

@Component
public class ProductController implements Initializable, CategoryObserver, ProductObserver {

    @FXML
    private Button addNewButton, resetFilterButton, exportFromExcelButton, detailProduct, addOrderButton;
    @FXML
    private ComboBox<String> filterCategories, filterProductType, filterSearchComboBox;
    @FXML
    private TableView<ProductDTO> productTableView;
    @FXML
    private TextField searchField;

    private ObservableList<ProductDTO> productList;

    @Autowired
    private ProductService productService;
    @Autowired
    private ViewFactory viewFactory;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryObservable categoryObservable;
    @Autowired
    private ProductObservable productObservable;

    @Autowired
    private SoftwareAccountService softwareAccountService;
    @Autowired
    private SoftwareLicenseKeyService softwareLicenseKeyService;
    @Setter
    private Stage stage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        productList = FXCollections.observableArrayList();
        productTableView.setItems(productList);
        setupTableView();
        loadAllData();
        setupSearchField();
        setupEventHandlers();
        categoryObservable.addObserver(this);
        productObservable.addObserver(this);
    }

    private void setupEventHandlers() {
        addNewButton.setOnAction(e -> addNewProduct());
        filterCategories.setOnAction(event -> applyFilters());
        filterProductType.setOnAction(event -> applyFilters());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        resetFilterButton.setOnAction(this::resetFilters);
        filterSearchComboBox.setOnAction(event -> applyFilters());
        exportFromExcelButton.setOnAction(this::exportToExcelAction);
        detailProduct.setOnAction(e -> {
            ProductDTO productDTO = productTableView.getSelectionModel().getSelectedItem();
            if (productDTO != null) {
                detailProduct(productDTO);
            }
        });
        addOrderButton.setOnAction(e -> {
            ProductDTO productDTO = productTableView.getSelectionModel().getSelectedItem();
            if (productDTO != null) {
                    if (productDTO.getQuantity() == 0) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be at least 1.");
                    } else {
                        viewFactory.showAddOrderView(productDTO);
                    }
            }
        });
    }

    private void exportToExcelAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            String excelFilePath = selectedFile.getAbsolutePath();
            try {
                productService.exportDataToExcel(excelFilePath);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data exported successfully");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export data to Excel");
            }
        }
    }

    @FXML
    private void resetFilters(ActionEvent actionEvent) {
        searchField.clear();
        filterCategories.setValue("Categories");
        filterProductType.setValue("Product Type");
        filterSearchComboBox.setValue("Everything");
        loadAllData();
    }

    private void setupTableView() {
        createColumn("ID", "productId", Long.class, 15);

        TableColumn<ProductDTO, ImageView> imageColumn = new TableColumn<>("Images");
        imageColumn.setCellValueFactory(data -> {
            String imageUrl = data.getValue().getImageUrl();
            File file = new File(imageUrl);
            ImageView imageView = (file.exists() && !file.isDirectory())
                    ? new ImageView(new Image("file:" + imageUrl))
                    : new ImageView(new Image("Images/account.png"));
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            return new SimpleObjectProperty<>(imageView);
        });
        imageColumn.setPrefWidth(50);
        productTableView.getColumns().add(imageColumn);

        createColumn("Name", "nameProduct", String.class);
        createColumn("Description", "description", String.class, 70);
        createColumn("Price", "price", BigDecimal.class, 30);
        createColumn("Quantity", "quantity", Integer.class, 30);
        createColumn("Status", "status", ProductStatus.class, 30);

        setupActionsColumn();
    }

    private <T> void createColumn(String title, String property, Class<T> type, double width) {
        TableColumn<ProductDTO, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        productTableView.getColumns().add(column);
    }

    private <T> void createColumn(String title, String property, Class<T> type) {
        TableColumn<ProductDTO, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        productTableView.getColumns().add(column);
    }

    private void setupActionsColumn() {
        TableColumn<ProductDTO, Void> actionsColumn = viewFactory.createActionColumnProduct(this::updateProduct, this::deleteProduct);
        productTableView.getColumns().add(actionsColumn);
        actionsColumn.setPrefWidth(40);
    }

    private void addNewProduct() {
        viewFactory.showAddNewProductView(this::loadAllProducts);
    }

    //show product details
    private void detailProduct(ProductDTO productDTO) {
        viewFactory.showProductDetailView(productDTO);
    }

    private void updateProduct(ProductDTO productDTO) {
        viewFactory.showUpdateProductView(productDTO, this::loadAllProducts);
    }

    private void deleteProduct(ProductDTO productDTO) {
        Optional<Boolean> result = AlertUtil.showConfirmDialog("Delete Product", "Are you sure you want to delete this product?");
        if (result.orElse(false)) {
            productService.deleteProduct(productDTO);
            productList.remove(productDTO);
        }
    }

    private void loadFilterCategories() {
        CompletableFuture.supplyAsync(categoryService::getAllCategories)
                .thenApplyAsync(result -> result.stream()
                        .map(CategoryDTO::getCategoryName)
                        .collect(Collectors.toList()))
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    result.add(0, "Categories");
                    filterCategories.setItems(FXCollections.observableArrayList(result));
                }));
        filterCategories.setValue("Categories");
    }

    private void loadFilterProductTypes() {
        Platform.runLater(() -> {
            filterProductType.getItems().clear();
            filterProductType.getItems().add("Product Type");
            for (ProductTypeCode productTypeCode : ProductTypeCode.values()) {
                filterProductType.getItems().add(productTypeCode.name());
            }
            filterProductType.setValue("Product Type");
        });
    }

    private void applyFilters() {
        String selectedCategory = filterCategories.getValue();
        String selectedProductType = filterProductType.getValue();
        String searchValue = searchField.getText().toLowerCase();
        String selectedSearch = filterSearchComboBox.getValue();

        CompletableFuture.supplyAsync(productService::getAllProductsWithImages)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    productList.clear();
                    productList.addAll(result.stream()
                            .filter(product -> filterCategory(product, selectedCategory))
                            .filter(product -> filterProductType(product, selectedProductType))
                            .filter(product -> filterSearch(product, selectedSearch, searchValue))
                            .toList());
                }));
    }

    private boolean filterCategory(ProductDTO product, String selectedCategory) {
        return selectedCategory == null || selectedCategory.equals("Categories") || product.getCategoryName().equals(selectedCategory);
    }

    private boolean filterProductType(ProductDTO product, String selectedProductType) {
        return selectedProductType == null || selectedProductType.equals("Product Type") || product.getProductType().name().equals(selectedProductType);
    }

    private boolean filterSearch(ProductDTO product, String selectedSearch, String searchValue) {
        return switch (selectedSearch) {
            case "Name Product" -> Optional.ofNullable(product.getNameProduct())
                    .map(name -> name.toLowerCase().contains(searchValue))
                    .orElse(false);
            case "Category" -> Optional.ofNullable(product.getCategoryName())
                    .map(category -> category.toLowerCase().contains(searchValue))
                    .orElse(false);
            case "Product Type" -> Optional.ofNullable(product.getProductType())
                    .map(type -> type.name().toLowerCase().contains(searchValue))
                    .orElse(false);
            default -> productMatchesSearch(product, searchValue);
        };
    }

    private boolean productMatchesSearch(ProductDTO product, String searchValue) {
        return Optional.ofNullable(product.getNameProduct())
                .map(name -> name.toLowerCase().contains(searchValue))
                .orElse(false) ||
                Optional.ofNullable(product.getCategoryName())
                        .map(category -> category.toLowerCase().contains(searchValue))
                        .orElse(false) ||
                Optional.ofNullable(product.getProductType())
                        .map(type -> type.name().toLowerCase().contains(searchValue))
                        .orElse(false);
    }

    private void setupSearchField() {
        filterSearchComboBox.getItems().addAll("Everything", "Name Product", "Category", "Product Type");
        filterSearchComboBox.setValue("Everything");
    }

    private void loadAllData() {
        loadAllProducts();
        loadFilterCategories();
        loadFilterProductTypes();
    }

    private void loadAllProducts() {
        CompletableFuture.supplyAsync(productService::getAllProductsWithImages)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    productList.clear();
                    productList.addAll(result);
                }));
    }

    @Override
    public void updateCategories() {
        loadFilterCategories();
        loadAllProducts();
    }

    @Override
    public void update() {
        loadAllProducts();
    }

    @Override
    public void updateProduct() {
        loadAllProducts();
    }
}

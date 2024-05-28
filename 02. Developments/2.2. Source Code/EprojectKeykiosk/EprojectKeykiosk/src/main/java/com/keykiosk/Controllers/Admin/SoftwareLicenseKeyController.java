package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;
import com.keykiosk.Models.EnumType.PaymentStatus;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Observer.ProductObservable;
import com.keykiosk.Observer.ProductObserver;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Services.SoftwareLicenseKeyService;
import com.keykiosk.Util.AlertUtil;
import com.keykiosk.Views.ViewFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.keykiosk.Util.AlertUtil.showAlert;
import static com.keykiosk.Util.RandomCodeUtil.generateUniqueRandomCode;

@Component
public class SoftwareLicenseKeyController implements Initializable, ProductObserver {
    @FXML
    private Button addNewButton;
    @FXML
    private Button exportToExcelButton;
    @FXML
    private Button importFromExcelButton;
    @FXML
    private TextField licenseKeyTextField;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> selectProductComboBox;
    @FXML
    private TableView<SoftwareLicenseKeyDTO> softwareLicenseKeyTableView;
    @FXML
    private ComboBox<String> filterPaymentStatusComboBox;
    @FXML
    private ComboBox<String> filterProductComboBox;
    @FXML
    private ComboBox<String> filterSearchComboBox;
    @FXML
    private Button resetFilterButton;

    private ObservableList<SoftwareLicenseKeyDTO> softwareLicenseKeyList;

    @FXML
    private Label totalKeyLabel;
    @FXML
    private Label outOfStockProductLabel;
    @FXML
    private Label PaymentFailedLabel;
    @FXML
    private Label paymentSuccessfulLabel;
    @Autowired
    private SoftwareLicenseKeyService softwareLicenseKeyService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ViewFactory viewFactory;
    @Autowired
    private ProductObservable productObservable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        softwareLicenseKeyList = FXCollections.observableArrayList();
        softwareLicenseKeyTableView.setItems(softwareLicenseKeyList);
        setupTableView();
        loadAllSoftwareLicenseKeys();
        setupEventHandlers();
        initializeFilterOptions();
        setLabelCounts();
        productObservable.addObserver(this);
    }

    private void setupEventHandlers() {
        addNewButton.setOnAction(this::insertSoftwareLicenseKey);
        filterProductComboBox.setOnAction(event -> applyFilters());
        filterPaymentStatusComboBox.setOnAction(event -> applyFilters());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        resetFilterButton.setOnAction(this::resetFilter);
        exportToExcelButton.setOnAction(this::exportToExcelAction);
        importFromExcelButton.setOnAction(this::importFromExcelAction);
    }

    private void setLabelCounts() {
        CompletableFuture<Void> softwareKeysFuture = CompletableFuture.supplyAsync(softwareLicenseKeyService::getAllSoftwareLicenseKeys)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    totalKeyLabel.setText(String.valueOf(result.size()));

                    long successfulCount = result.stream()
                            .filter(key -> key.getPaymentStatus() == PaymentStatus.SUCCESSFUL)
                            .count();
                    long failedCount = result.stream()
                            .filter(key -> key.getPaymentStatus() == PaymentStatus.FAILED)
                            .count();

                    paymentSuccessfulLabel.setText(String.valueOf(successfulCount));
                    PaymentFailedLabel.setText(String.valueOf(failedCount));
                }));

        CompletableFuture<Void> productsFuture = CompletableFuture.supplyAsync(productService::getAllProductsWithImages)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    long outOfStockCount = result.stream()
                            .filter(product -> product.getProductType() == ProductTypeCode.KEY && product.getQuantity() == 0)
                            .count();
                    outOfStockProductLabel.setText(String.valueOf(outOfStockCount));
                }));

        CompletableFuture.allOf(softwareKeysFuture, productsFuture).join();
    }

    private void initializeFilterOptions() {
        loadAllProductsToComboBox();
        loadFilterProduct();
        loadFilterPaymentStatus();
        setupSearchField();
        filterProductComboBox.setValue("Product");
        filterPaymentStatusComboBox.setValue("Payment Status");
    }

    private void applyFilters() {
        String selectedProduct = filterProductComboBox.getValue();
        String selectedPaymentStatus = filterPaymentStatusComboBox.getValue();
        String searchValue = searchField.getText().toLowerCase();
        String selectedSearch = filterSearchComboBox.getValue();

        CompletableFuture.supplyAsync(softwareLicenseKeyService::getAllSoftwareLicenseKeys)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    softwareLicenseKeyList.setAll(result.stream()
                            .filter(key -> !key.isDeleted()) // Filter out deleted keys
                            .filter(key -> filterProduct(key, selectedProduct))
                            .filter(key -> filterPaymentStatus(key, selectedPaymentStatus))
                            .filter(key -> filterSearch(key, selectedSearch, searchValue))
                            .collect(Collectors.toList()));
                }));
    }

    private boolean filterSearch(SoftwareLicenseKeyDTO key, String selectedSearch, String searchValue) {
        return switch (selectedSearch) {
            case "Code ID" -> key.getCodeId().toLowerCase().contains(searchValue);
            case "License Key" -> key.getLicenseKey().toLowerCase().contains(searchValue);
            case "Product Name" -> key.getNameProduct().toLowerCase().contains(searchValue);
            case "Payment Status" -> key.getPaymentStatus().name().toLowerCase().contains(searchValue);
            default -> key.getCodeId().toLowerCase().contains(searchValue) ||
                    key.getLicenseKey().toLowerCase().contains(searchValue) ||
                    key.getNameProduct().toLowerCase().contains(searchValue) ||
                    key.getPaymentStatus().name().toLowerCase().contains(searchValue);
        };
    }

    private boolean filterPaymentStatus(SoftwareLicenseKeyDTO key, String selectedPaymentStatus) {
        return selectedPaymentStatus == null || "Payment Status".equals(selectedPaymentStatus) || key.getPaymentStatus().name().equals(selectedPaymentStatus);
    }

    private boolean filterProduct(SoftwareLicenseKeyDTO key, String selectedProduct) {
        return selectedProduct == null || "Product".equals(selectedProduct) || key.getNameProduct().equals(selectedProduct);
    }

    private void loadFilterProduct() {
        CompletableFuture.supplyAsync(productService::getAllProducts)
                .thenApplyAsync(result -> result.stream()
                        .map(ProductDTO::getNameProduct)
                        .collect(Collectors.toList()))
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    result.add(0, "Product");
                    filterProductComboBox.setItems(FXCollections.observableArrayList(result));
                    filterProductComboBox.setValue("Product");
                }));
    }

    private void loadFilterPaymentStatus() {
        Platform.runLater(() -> {
            filterPaymentStatusComboBox.getItems().clear();
            filterPaymentStatusComboBox.getItems().add("Payment Status");
            for (PaymentStatus status : PaymentStatus.values()) {
                filterPaymentStatusComboBox.getItems().add(status.name());
            }
            filterPaymentStatusComboBox.setValue("Payment Status");
        });
    }

    private void loadAllSoftwareLicenseKeys() {
        CompletableFuture.supplyAsync(softwareLicenseKeyService::getAllSoftwareLicenseKeys)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    softwareLicenseKeyList.setAll(result.stream()
                            .filter(key -> !key.isDeleted()) // Filter out deleted keys
                            .collect(Collectors.toList()));
                }));
    }

    private void setupTableView() {
        createColumn("Code ID", "codeId", String.class, 15);
        createColumn("License Key", "licenseKey", String.class);
        createColumn("Product Name", "nameProduct", String.class, 30);
        createColumn("Payment Status", "paymentStatus", String.class, 30);
        setupActionsColumn();
    }

    private <T> void createColumn(String title, String property, Class<T> type, double width) {
        TableColumn<SoftwareLicenseKeyDTO, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        softwareLicenseKeyTableView.getColumns().add(column);
    }

    private <T> void createColumn(String title, String property, Class<T> type) {
        TableColumn<SoftwareLicenseKeyDTO, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        softwareLicenseKeyTableView.getColumns().add(column);
    }

    private void setupActionsColumn() {
        TableColumn<SoftwareLicenseKeyDTO, Void> actionsColumn = viewFactory.createActionColumnSoftwareLicenseKey(this::updateSoftwareLicenseKey, this::deleteSoftwareLicenseKey);
        softwareLicenseKeyTableView.getColumns().add(actionsColumn);
        actionsColumn.setPrefWidth(20);
    }

    private void deleteSoftwareLicenseKey(SoftwareLicenseKeyDTO key) {
        Optional<Boolean> result = AlertUtil.showConfirmDialog("Delete Key", "Are you sure you want to delete this key?");
        if (result.isPresent() && result.get()) {
            key.setDeleted(true); // Set deleted to true
            softwareLicenseKeyService.updateKey(key); // Update the key in the database
            applyFilters(); // Refresh the table view
            productObservable.notifyProductObservers();
        }
    }

    private void updateSoftwareLicenseKey(SoftwareLicenseKeyDTO key) {
        viewFactory.showUpdateDialogSoftwareLicenseKey(key, this::loadAllSoftwareLicenseKeys);
    }

    private void importFromExcelAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String excelFilePath = selectedFile.getAbsolutePath();
            try {
                softwareLicenseKeyService.importDataFromExcel(excelFilePath);
                loadAllSoftwareLicenseKeys();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data imported successfully");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to import data from Excel");
            }
        }
    }

    private void exportToExcelAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            String excelFilePath = selectedFile.getAbsolutePath();
            try {
                softwareLicenseKeyService.exportDataToExcel(excelFilePath);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data exported successfully");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export data to Excel");
            }
        }
    }

    private void insertSoftwareLicenseKey(ActionEvent actionEvent) {
        if (!validateInput()) {
            return;
        }

        SoftwareLicenseKeyDTO key = new SoftwareLicenseKeyDTO();
        key.setAccountId(Model.getInstance().getLoggedInUser().getAccountId());
        key.setProductId(productService.getProductByName(selectProductComboBox.getValue()).getProductId());
        key.setLicenseKey(licenseKeyTextField.getText());
        key.setCodeId(generateUniqueRandomCode());
        key.setPaymentStatus(PaymentStatus.PENDING);
        key.setStatus(Status.ACTIVE);
        try {
            softwareLicenseKeyService.createSoftwareLicenseKey(key);
            loadAllSoftwareLicenseKeys();
            productObservable.notifyProductObservers();
            clearFields();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to insert product");
        }
    }

    private boolean validateInput() {
        if (selectProductComboBox.getValue() == null || selectProductComboBox.getValue().equals("--Select product--")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a product");
            return false;
        }

        if (licenseKeyTextField.getText() == null || licenseKeyTextField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a license key");
            return false;
        }

        return true;
    }

    private void setupSearchField() {
        filterSearchComboBox.getItems().addAll("Everything", "Code ID", "License Key", "Product Name", "Payment Status");
        filterSearchComboBox.setValue("Everything");
    }

    private void clearFields() {
        licenseKeyTextField.clear();
        selectProductComboBox.setValue("--Select Product--");
        selectProductComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void resetFilter(ActionEvent actionEvent) {
        loadFilterProduct();
        loadFilterPaymentStatus();
        searchField.clear();
        filterSearchComboBox.setValue("Everything");
        selectProductComboBox.setValue("--Select product--");
        applyFilters();
    }

    private void loadAllProductsToComboBox() {
        CompletableFuture.supplyAsync(productService::getAllProducts)
                .thenApplyAsync(result -> result.stream()
                        .filter(product -> product.getProductType() == ProductTypeCode.KEY)
                        .collect(Collectors.toList()))
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    selectProductComboBox.getItems().clear();
                    selectProductComboBox.getItems().addAll(result.stream()
                            .map(ProductDTO::getNameProduct)
                            .collect(Collectors.toList()));
                    selectProductComboBox.setValue("--Select product--");
                }));
    }

    @Override
    public void updateProduct() {
        loadAllProductsToComboBox();
        loadAllSoftwareLicenseKeys();
        setLabelCounts();
    }
}


package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.EnumType.PaymentStatus;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Observer.ProductObservable;
import com.keykiosk.Observer.ProductObserver;
import com.keykiosk.Services.ProductService;
import com.keykiosk.Services.SoftwareAccountService;
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
public class SoftwareAccountController implements Initializable, ProductObserver {
    @FXML
    private Button addNewButton;
    @FXML
    private Button exportToExcelButton;
    @FXML
    private Button importFromExcelButton;
    @FXML
    private TextField accountInfoTextField;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> selectProductComboBox;
    @FXML
    private TableView<SoftwareAccountDTO> softwareAccountTableView;
    @FXML
    private ComboBox<String> filterPaymentStatusComboBox;
    @FXML
    private ComboBox<String> filterProductComboBox;
    @FXML
    private ComboBox<String> filterSearchComboBox;
    @FXML
    private Button resetFilterButton;

    private ObservableList<SoftwareAccountDTO> softwareAccountList;

    @FXML
    private Label totalAccountLabel;
    @FXML
    private Label outOfStockProductLabel;
    @FXML
    private Label PaymentFailedLabel;
    @FXML
    private Label paymentSuccessfulLabel;
    @Autowired
    private SoftwareAccountService softwareAccountService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ViewFactory viewFactory;
    @Autowired
    private ProductObservable productObservable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        softwareAccountList = FXCollections.observableArrayList();
        softwareAccountTableView.setItems(softwareAccountList);
        setupTableView();
        loadAllSoftwareAccounts();
        setupEventHandlers();
        initializeFilterOptions();
        setLabelCounts();
        productObservable.addObserver(this);
    }

    private void setupEventHandlers() {
        addNewButton.setOnAction(this::insertSoftwareAccount);
        filterProductComboBox.setOnAction(event -> applyFilters());
        filterPaymentStatusComboBox.setOnAction(event -> applyFilters());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        resetFilterButton.setOnAction(this::resetFilter);
        exportToExcelButton.setOnAction(this::exportToExcelAction);
        importFromExcelButton.setOnAction(this::importFromExcelAction);
    }

    private void setLabelCounts() {
        CompletableFuture<Void> softwareAccountsFuture = CompletableFuture.supplyAsync(softwareAccountService::getAllSoftwareAccounts)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    totalAccountLabel.setText(String.valueOf(result.size()));

                    long successfulCount = result.stream()
                            .filter(account -> account.getPaymentStatus() == PaymentStatus.SUCCESSFUL)
                            .count();
                    long failedCount = result.stream()
                            .filter(account -> account.getPaymentStatus() == PaymentStatus.FAILED)
                            .count();

                    paymentSuccessfulLabel.setText(String.valueOf(successfulCount));
                    PaymentFailedLabel.setText(String.valueOf(failedCount));
                }));

        CompletableFuture<Void> productsFuture = CompletableFuture.supplyAsync(productService::getAllProductsWithImages)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    long outOfStockCount = result.stream()
                            .filter(product -> product.getProductType() == ProductTypeCode.ACCOUNT && product.getQuantity() == 0)
                            .count();
                    outOfStockProductLabel.setText(String.valueOf(outOfStockCount));
                }));

        CompletableFuture.allOf(softwareAccountsFuture, productsFuture).join();
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

        CompletableFuture.supplyAsync(softwareAccountService::getAllSoftwareAccounts)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    softwareAccountList.setAll(result.stream()
                            .filter(account -> filterProduct(account, selectedProduct))
                            .filter(account -> filterPaymentStatus(account, selectedPaymentStatus))
                            .filter(account -> filterSearch(account, selectedSearch, searchValue))
                            .collect(Collectors.toList()));
                }));
    }

    private boolean filterSearch(SoftwareAccountDTO account, String selectedSearch, String searchValue) {
        return switch (selectedSearch) {
            case "Code ID" -> account.getCodeId().toLowerCase().contains(searchValue);
            case "Account Info" -> account.getAccountInfo().toLowerCase().contains(searchValue);
            case "Product Name" -> account.getNameProduct().toLowerCase().contains(searchValue);
            case "Payment Status" -> account.getPaymentStatus().name().toLowerCase().contains(searchValue);
            default -> account.getCodeId().toLowerCase().contains(searchValue) ||
                    account.getAccountInfo().toLowerCase().contains(searchValue) ||
                    account.getNameProduct().toLowerCase().contains(searchValue) ||
                    account.getPaymentStatus().name().toLowerCase().contains(searchValue);
        };
    }

    private boolean filterPaymentStatus(SoftwareAccountDTO account, String selectedPaymentStatus) {
        return selectedPaymentStatus == null || "Payment Status".equals(selectedPaymentStatus) || account.getPaymentStatus().name().equals(selectedPaymentStatus);
    }

    private boolean filterProduct(SoftwareAccountDTO account, String selectedProduct) {
        return selectedProduct == null || "Product".equals(selectedProduct) || account.getNameProduct().equals(selectedProduct);
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

    private void loadAllSoftwareAccounts() {
        CompletableFuture.supplyAsync(softwareAccountService::getAllSoftwareAccounts)
                .thenAcceptAsync(result -> Platform.runLater(() -> {
                    softwareAccountList.setAll(result);
                }));
    }

    private void setupTableView() {
        createColumn("Code ID", "codeId", String.class, 15);
        createColumn("Account Info", "accountInfo", String.class);
        createColumn("Product Name", "nameProduct", String.class, 30);
        createColumn("Payment Status", "paymentStatus", String.class, 30);
        setupActionsColumn();
    }

    private <T> void createColumn(String title, String property, Class<T> type, double width) {
        TableColumn<SoftwareAccountDTO, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        softwareAccountTableView.getColumns().add(column);
    }

    private <T> void createColumn(String title, String property, Class<T> type) {
        TableColumn<SoftwareAccountDTO, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        softwareAccountTableView.getColumns().add(column);
    }

    private void setupActionsColumn() {
        TableColumn<SoftwareAccountDTO, Void> actionsColumn = viewFactory.createActionColumnSoftwareAccount(this::updateSoftwareAccount, this::deleteSoftwareAccount);
        softwareAccountTableView.getColumns().add(actionsColumn);
        actionsColumn.setPrefWidth(20);
    }

    private void deleteSoftwareAccount(SoftwareAccountDTO account) {
        Optional<Boolean> result = AlertUtil.showConfirmDialog("Delete Account", "Are you sure you want to delete this account?");
        if (result.isPresent() && result.get()) {
            softwareAccountService.deleteSoftwareAccountDTO(account);
            softwareAccountList.remove(account);
            productObservable.notifyProductObservers();
        }
    }

    private void updateSoftwareAccount(SoftwareAccountDTO account) {
        // viewFactory.showUpdateDialogSoftwareAccount(account, this::loadAllSoftwareAccounts);
    }

    private void importFromExcelAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String excelFilePath = selectedFile.getAbsolutePath();
            try {
                softwareAccountService.importDataFromExcel(excelFilePath);
                loadAllSoftwareAccounts();
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
                softwareAccountService.exportDataToExcel(excelFilePath);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data exported successfully");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export data to Excel");
            }
        }
    }

    private void insertSoftwareAccount(ActionEvent actionEvent) {
        if (!validateInput()) {
            return;
        }

        SoftwareAccountDTO account = new SoftwareAccountDTO();
        account.setAccountId(Model.getInstance().getLoggedInUser().getAccountId());
        account.setProductId(productService.getProductByName(selectProductComboBox.getValue()).getProductId());
        account.setAccountInfo(accountInfoTextField.getText());
        account.setCodeId(generateUniqueRandomCode());
        account.setPaymentStatus(PaymentStatus.PENDING);
        account.setStatus(Status.ACTIVE);
        try {
            softwareAccountService.createSoftwareAccount(account);
            loadAllSoftwareAccounts();
            productObservable.notifyProductObservers();
            clearFields();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to insert account");
        }
    }

    private boolean validateInput() {
        if (selectProductComboBox.getValue() == null || selectProductComboBox.getValue().equals("--Select product--")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a product");
            return false;
        }

        if (accountInfoTextField.getText() == null || accountInfoTextField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter account info");
            return false;
        }

        return true;
    }

    private void setupSearchField() {
        filterSearchComboBox.getItems().addAll("Everything", "Code ID", "Account Info", "Product Name", "Payment Status");
        filterSearchComboBox.setValue("Everything");
    }

    private void clearFields() {
        accountInfoTextField.clear();
        selectProductComboBox.setValue("--Select product--");
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
                        .filter(product -> product.getProductType() == ProductTypeCode.ACCOUNT)
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
        loadAllSoftwareAccounts();
        setLabelCounts();
    }
}

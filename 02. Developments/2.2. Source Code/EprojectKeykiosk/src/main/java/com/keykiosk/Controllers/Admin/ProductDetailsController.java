package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.ProductDTO;
import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Services.SoftwareAccountService;
import com.keykiosk.Services.SoftwareLicenseKeyService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class ProductDetailsController implements Initializable {

    @FXML
    private Label descriptionProductLabel, namProductLabel, priceProductLabel, quantityProductLabel, statusProductLabel;
    @FXML
    private ComboBox<String> filterSearchComboBox;
    @FXML
    private ImageView imageViewProduct;
    @FXML
    private TableView<Object> productListTableView;
    @FXML
    private TextField searchField;

    private ProductDTO productToUpdate;
    private ObservableList<SoftwareLicenseKeyDTO> softwareLicenseKeyList;
    private ObservableList<SoftwareAccountDTO> softwareAccountList;

    @Autowired
    private SoftwareAccountService softwareAccountService;
    @Autowired
    private SoftwareLicenseKeyService softwareLicenseKeyService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        softwareLicenseKeyList = FXCollections.observableArrayList();
        softwareAccountList = FXCollections.observableArrayList();
        setupSearchField();
    }

    public void setProductDTO(ProductDTO productDTO) {
        this.productToUpdate = productDTO;
        updateProductLabels(productDTO);
        loadProductData(productDTO);
        setupTableView();
        setupSearchField();
    }

    private void updateProductLabels(ProductDTO productDTO) {
        namProductLabel.setText(productDTO.getNameProduct());
        priceProductLabel.setText(productDTO.getPrice().toString());
        descriptionProductLabel.setText(productDTO.getDescription());
        statusProductLabel.setText(productDTO.getStatus().toString());
        quantityProductLabel.setText(productDTO.getQuantity().toString());
        loadImage(productDTO.getImageUrl());
    }

    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            File imageFile = new File(imageUrl);
            imageViewProduct.setImage(imageFile.exists() ? new Image(imageFile.toURI().toString()) : null);
        } else {
            imageViewProduct.setImage(null);
        }
    }

    private void loadProductData(ProductDTO productDTO) {
        switch (productDTO.getProductType()) {
            case ACCOUNT -> softwareAccountList.setAll(softwareAccountService.findByProduct_NameProduct(productDTO.getNameProduct()));
            case KEY -> softwareLicenseKeyList.setAll(softwareLicenseKeyService.findByProduct_NameProducts(productDTO.getNameProduct()));
        }
    }

    private void setupTableView() {
        productListTableView.getColumns().clear();
        if (productToUpdate != null && productToUpdate.getProductType() != null) {
            if (productToUpdate.getProductType() == ProductTypeCode.ACCOUNT) {
                setupSoftwareAccountTable();
                productListTableView.setItems((ObservableList) softwareAccountList);
            } else if (productToUpdate.getProductType() == ProductTypeCode.KEY) {
                setupSoftwareLicenseKeyTable();
                productListTableView.setItems((ObservableList) softwareLicenseKeyList);
            }
        }
    }

    private void setupSoftwareAccountTable() {
        createColumn("ID", "softwareAccountId", Long.class, 0);
        createColumn("Account Info", "accountInfo", String.class, 50);
    }

    private void setupSoftwareLicenseKeyTable() {
        createColumn("Code ID", "codeId", String.class, 0);
        createColumn("License Key", "licenseKey", String.class,50);
    }

    private <T> void createColumn(String title, String property, Class<T> type) {
        TableColumn<Object, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        productListTableView.getColumns().add(column);
    }

    private <T> void createColumn(String title, String property, Class<T> type, double width) {
        TableColumn<Object, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        productListTableView.getColumns().add(column);
    }

    private void setupSearchField() {
        filterSearchComboBox.getItems().clear();
        if (productToUpdate != null && productToUpdate.getProductType() != null) {
            if (productToUpdate.getProductType() == ProductTypeCode.ACCOUNT) {
                filterSearchComboBox.getItems().addAll("Everything", "Account Info");
            } else if (productToUpdate.getProductType() == ProductTypeCode.KEY) {
                filterSearchComboBox.getItems().addAll("Everything", "Code ID", "License Key");
            }
            filterSearchComboBox.setValue("Everything");
        }

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterData());
        filterSearchComboBox.valueProperty().addListener((observable, oldValue, newValue) -> filterData());
    }

    @FXML
    private void filterData() {
        final String filterCriteria = filterSearchComboBox.getValue();
        final String searchText = searchField.getText().toLowerCase();

        if (filterCriteria == null || searchText == null) {
            return;
        }

        if (productToUpdate != null) {
            if (productToUpdate.getProductType() == ProductTypeCode.ACCOUNT) {
                ObservableList<SoftwareAccountDTO> filteredAccounts = softwareAccountList.filtered(account -> filterAccount(account, filterCriteria, searchText));
                productListTableView.setItems((ObservableList) filteredAccounts);
            } else if (productToUpdate.getProductType() == ProductTypeCode.KEY) {
                ObservableList<SoftwareLicenseKeyDTO> filteredKeys = softwareLicenseKeyList.filtered(key -> filterKey(key, filterCriteria, searchText));
                productListTableView.setItems((ObservableList) filteredKeys);
            }
        }
    }

    private boolean filterAccount(SoftwareAccountDTO account, String filterCriteria, String searchText) {
        return switch (filterCriteria) {
            case "Account Info", "Everything" -> account.getAccountInfo() != null && account.getAccountInfo().toLowerCase().contains(searchText);
            default -> false;
        };
    }

    private boolean filterKey(SoftwareLicenseKeyDTO key, String filterCriteria, String searchText) {
        return switch (filterCriteria) {
            case "Code ID" -> key.getCodeId() != null && key.getCodeId().toLowerCase().contains(searchText);
            case "License Key" -> key.getLicenseKey() != null && key.getLicenseKey().toLowerCase().contains(searchText);
            case "Everything" -> (key.getCodeId() != null && key.getCodeId().toLowerCase().contains(searchText)) ||
                    (key.getLicenseKey() != null && key.getLicenseKey().toLowerCase().contains(searchText));
            default -> false;
        };
    }
}

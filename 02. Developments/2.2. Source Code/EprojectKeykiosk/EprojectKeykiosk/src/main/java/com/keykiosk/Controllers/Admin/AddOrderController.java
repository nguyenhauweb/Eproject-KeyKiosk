package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.*;
import com.keykiosk.Models.EnumType.OrderStatus;
import com.keykiosk.Models.EnumType.PaymentMethod;
import com.keykiosk.Models.EnumType.ProductTypeCode;
import com.keykiosk.Models.Model;
import com.keykiosk.Observer.OderObservable;
import com.keykiosk.Observer.ProductObservable;
import com.keykiosk.Services.*;
import com.keykiosk.Util.RandomCodeUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.keykiosk.Util.AlertUtil.showAlert;

@Component
public class AddOrderController implements Initializable {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private SoftwareAccountService softwareAccountService;
    @Autowired
    private SoftwareLicenseKeyService softwareLicenseKeyService;
    @Autowired
    private OderObservable oderObservable;
    @Autowired
    private ProductObservable productObservable;

    @FXML
    private TextField quantityField;
    @FXML
    private Label productNameLabel;
    @FXML
    private Button addOrderButton;
    @FXML
    private ComboBox<String> paymentMethodComboBox;
    @FXML
    private Button addQuantityOrder;
    @FXML
    private Button subtractQuantityOrder;
    @FXML
    private Button cancelButton;
    @FXML
    private ImageView imageViewProduct;
    @FXML
    private Label priceProductLabel;
    @FXML
    private Label totalAmountOrderLabel;

    private ProductDTO productDTO;
    private List<SoftwareAccountDTO> selectedAccounts;
    private List<SoftwareLicenseKeyDTO> selectedKeys;
    @Setter
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPaymentMethod();
        addOrderButton.setOnAction(event -> createOrder());
        addQuantityOrder.setOnAction(event -> changeQuantity(1));
        subtractQuantityOrder.setOnAction(event -> changeQuantity(-1));
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateQuantityInput();
            updateTotalAmount();
        });
        cancelButton.setOnAction(event -> stage.close());
    }

    public void setProductDTO(ProductDTO productDTO) {
        this.productDTO = productDTO;
        productNameLabel.setText(productDTO.getNameProduct());
        priceProductLabel.setText(productDTO.getPrice().toString());
        loadImage(productDTO.getImageUrl());
        updateTotalAmount();
    }

    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            File imageFile = new File(imageUrl);
            imageViewProduct.setImage(imageFile.exists() ? new Image(imageFile.toURI().toString()) : null);
        } else {
            imageViewProduct.setImage(null);
        }
    }

    private void changeQuantity(int delta) {
        try {
            int currentQuantity = Integer.parseInt(quantityField.getText());
            currentQuantity += delta;
            if (currentQuantity < 1) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be at least 1.");
                currentQuantity = 1;
            } else if (currentQuantity > productDTO.getQuantity()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity exceeds available stock.");
                currentQuantity = productDTO.getQuantity();
            }
            quantityField.setText(String.valueOf(currentQuantity));
        } catch (NumberFormatException e) {
            quantityField.setText("1");
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for quantity.");
        }
        updateTotalAmount();
    }

    private void validateQuantityInput() {
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity < 1) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be at least 1.");
                quantityField.setText("1");
            } else if (quantity > productDTO.getQuantity()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity exceeds available stock.");
                quantityField.setText(String.valueOf(productDTO.getQuantity()));
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for quantity.");
            quantityField.setText("1");
        }
    }

    private void updateTotalAmount() {
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            BigDecimal totalAmount = productDTO.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmountOrderLabel.setText(totalAmount.toString());
        } catch (NumberFormatException e) {
            totalAmountOrderLabel.setText("0");
        }
    }

    private void createOrder() {
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for quantity.");
            return;
        }

        if (quantity < 1) {
            showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be at least 1.");
            return;
        }

        if (quantity > productDTO.getQuantity()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity exceeds available stock.");
            return;
        }

        if (paymentMethodComboBox.getValue() == null || paymentMethodComboBox.getValue().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please select a payment method.");
            return;
        }

        if (!validateAndSetSelectedItems(quantity)) return;

        try {
            OrderDTO orderDTO = buildOrderDTO(quantity);
            orderService.createOrder(orderDTO);
            removeSelectedItems();
            oderObservable.notifyUser();
            stage.close();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order created successfully");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create order: " + e.getMessage());
        }
    }

    private boolean validateAndSetSelectedItems(int quantity) {
        if (productDTO == null) return false;

        switch (productDTO.getProductType()) {
            case ACCOUNT:
                selectedAccounts = softwareAccountService.findAccountsWithMinIdByProduct(productDTO.getNameProduct(), quantity);
                if (selectedAccounts.size() < quantity) {
                    showAlert(Alert.AlertType.ERROR, "Insufficient Quantity", "There are only " + selectedAccounts.size() + " accounts available.");
                    return false;
                }
                break;
            case KEY:
                selectedKeys = softwareLicenseKeyService.findKeysWithMinIdByProduct(productDTO.getNameProduct(), quantity);
                if (selectedKeys.size() < quantity) {
                    showAlert(Alert.AlertType.ERROR, "Insufficient Quantity", "There are only " + selectedKeys.size() + " keys available.");
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private OrderDTO buildOrderDTO(int quantity) {
        BigDecimal totalAmount = productDTO.getPrice().multiply(BigDecimal.valueOf(quantity));

        return OrderDTO.builder()
                .productId(productDTO.getProductId())
                .accountId(Model.getInstance().getLoggedInUser().getAccountId())
                .quantity(quantity)
                .codeId(RandomCodeUtil.generateUniqueRandomCode())
                .paymentMethod(PaymentMethod.valueOf(paymentMethodComboBox.getValue()))
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.SUCCESS)
                .productType(productDTO.getProductType())
                .build();
    }

    private void removeSelectedItems() {
        if (productDTO.getProductType() == ProductTypeCode.ACCOUNT) {
            selectedAccounts.forEach(softwareAccountService::deleteSoftwareAccountDTO);
        } else if (productDTO.getProductType() == ProductTypeCode.KEY) {
            selectedKeys.forEach(softwareLicenseKeyService::deleteKey);
        }
    }

    private void loadPaymentMethod() {
        for (PaymentMethod paymentMethod : PaymentMethod.values()) {
            paymentMethodComboBox.getItems().add(paymentMethod.name());
        }
    }
}

package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.DTO.OrderDTO;
import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;
import com.keykiosk.Observer.OderObservable;
import com.keykiosk.Observer.OderObserver;
import com.keykiosk.Services.OrderService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@Component
public class OrderController implements Initializable, OderObserver {

    @FXML
    private TableView<OrderDTO> orderTableView;
    @FXML
    private Button exportInvoiceToPDF;

    private ObservableList<OrderDTO> orderList;

    @Autowired
    private OrderService orderService;
    @Autowired
    private OderObservable oderObservable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderList = FXCollections.observableArrayList();
        orderTableView.setItems(orderList);
        setupTableView();
        loadAllOrders();
        oderObservable.addObserver(this);

        exportInvoiceToPDF.setOnAction(this::handleExportInvoiceToPDF);
    }

    private void loadAllOrders() {
        CompletableFuture.supplyAsync(() -> orderService.getAllOrders())
                .thenAccept(orders -> {
                    Platform.runLater(() -> {
                        orderList.clear();
                        orderList.addAll(orders);
                    });
                });
    }

    private void setupTableView() {
        createColumn("OrderID", "codeId", String.class);
        createColumn("Product", "nameProduct", String.class);
        createColumn("Quantity", "quantity", Integer.class);
        createColumn("Total Amount", "totalAmount", Double.class);
        createColumn("Payment Method", "paymentMethod", String.class);
        createColumn("Order Status", "orderStatus", String.class);
        TableColumn<OrderDTO, String> orderDateColumn = new TableColumn<>("Order Date");
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime orderDate = cellData.getValue().getOrderDate();
            String formattedDate = (orderDate != null) ? orderDate.format(DateTimeFormatter.ofPattern("h:mm a M/d/yyyy")) : null;
            return new SimpleStringProperty(formattedDate);
        });

        orderTableView.getColumns().add(orderDateColumn);

    }

    private <T> void createColumn(String title, String property, Class<T> type) {
        TableColumn<OrderDTO, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        orderTableView.getColumns().add(column);
    }

    @Override
    public void updateOder() {
        loadAllOrders();
    }

    private void handleExportInvoiceToPDF(ActionEvent event) {
        OrderDTO selectedOrder = orderTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("invoice.pdf");
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                List<SoftwareAccountDTO> selectedAccounts = orderService.getDeletedAccountsByProductIdAndQuantity(selectedOrder.getProductId(), selectedOrder.getQuantity());
                List<SoftwareLicenseKeyDTO> selectedKeys = orderService.getDeletedKeysByProductIdAndQuantity(selectedOrder.getProductId(), selectedOrder.getQuantity());
                orderService.exportOrderToPDF(selectedOrder, file.getAbsolutePath(), selectedAccounts, selectedKeys);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Invoice exported successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export invoice: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

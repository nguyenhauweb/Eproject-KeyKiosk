package com.keykiosk.Controllers.Admin;

import com.keykiosk.Models.Model;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class AdminController implements Initializable {
    public BorderPane admin_parent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Model.getInstance().getViewFactory().getClientSelectedMenuItem().addListener((observableValue, oldValue, newValue) -> {
            switch (newValue) {
                case TRANSACTIONS -> admin_parent.setCenter(Model.getInstance().getViewFactory().getTransactionsView());
                case ACCOUNTS -> admin_parent.setCenter(Model.getInstance().getViewFactory().getAccountsView());
                case CATEGORY -> admin_parent.setCenter(Model.getInstance().getViewFactory().getCategoryView());
                case PRODUCT_TYPE -> admin_parent.setCenter(Model.getInstance().getViewFactory().getProductTypeView());
                case SOFTWARE_ACCOUNTS -> admin_parent.setCenter(Model.getInstance().getViewFactory().getSoftwareAccountsView());
                case SOFTWARE_LICENSES -> admin_parent.setCenter(Model.getInstance().getViewFactory().getSoftwareLicensesView());
                case PRODUCTS -> admin_parent.setCenter(Model.getInstance().getViewFactory().getProductView());
                case ORDERS -> admin_parent.setCenter(Model.getInstance().getViewFactory().getOrdersView());
                case PROFILE -> admin_parent.setCenter(Model.getInstance().getViewFactory().getProfileView());
                default -> admin_parent.setCenter(Model.getInstance().getViewFactory().getDashboardView());
            }
        });

    }



}

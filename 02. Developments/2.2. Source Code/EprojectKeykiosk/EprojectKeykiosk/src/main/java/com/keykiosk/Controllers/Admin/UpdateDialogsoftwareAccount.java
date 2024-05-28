package com.keykiosk.Controllers.Admin;


import com.jfoenix.controls.JFXButton;
import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Services.SoftwareAccountService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

import static com.keykiosk.Util.AlertUtil.showAlert;

@Component
public class UpdateDialogsoftwareAccount implements Initializable {

    @FXML
    private JFXButton CloseButton;

    @FXML
    private ComboBox<String> statusComboBox;

    @Setter
    private Stage stage;

    @FXML
    private JFXButton UpdateButton;

    private SoftwareAccountDTO softwareAccountToUpdate;

    @Autowired
    private SoftwareAccountService softwareAccountService;
    @FXML
    private TextArea accountInfoTextArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UpdateButton.setOnAction(this::updateSoftwareAccount);
        CloseButton.setOnAction(this::closeDialog);
        loadStatusToComboBox();
    }

    public void setUserDTO(SoftwareAccountDTO softwareAccountDTO) {
        this.softwareAccountToUpdate = softwareAccountDTO;
        this.accountInfoTextArea.setText(softwareAccountDTO.getAccountInfo());
        this.statusComboBox.setValue(softwareAccountDTO.getStatus().toString());
    }

    @Setter
    private Runnable afterUpdate;

    private void updateSoftwareAccount(ActionEvent event) {
        softwareAccountToUpdate.setAccountId(Model.getInstance().getLoggedInUser().getAccountId());
        softwareAccountToUpdate.setPaymentStatus(softwareAccountToUpdate.getPaymentStatus());
        softwareAccountToUpdate.setAccountInfo(accountInfoTextArea.getText());
        softwareAccountToUpdate.setStatus(Status.valueOf(statusComboBox.getValue()));
        softwareAccountToUpdate.setProductId(softwareAccountToUpdate.getProductId());

        try {
            softwareAccountService.updateSoftwareAccount(softwareAccountToUpdate);
            clearFields();
            if (afterUpdate != null) afterUpdate.run();
            Model.getInstance().getViewFactory().closeStage(stage);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error while updating software account");
        }
    }

    private void clearFields() {
        accountInfoTextArea.clear();
        statusComboBox.setValue(null);
    }

    private void loadStatusToComboBox() {
        for (Status status : Status.values()) {
            statusComboBox.getItems().add(status.name());
        }
    }


    private void closeDialog(ActionEvent event) {
        Model.getInstance().getViewFactory().closeStage(stage);
    }


}
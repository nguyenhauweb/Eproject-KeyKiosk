package com.keykiosk.Controllers.Admin;


import com.jfoenix.controls.JFXButton;
import com.keykiosk.Models.DTO.SoftwareAccountDTO;
import com.keykiosk.Models.DTO.SoftwareLicenseKeyDTO;
import com.keykiosk.Models.EnumType.Status;
import com.keykiosk.Models.Model;
import com.keykiosk.Services.SoftwareAccountService;
import com.keykiosk.Services.SoftwareLicenseKeyService;
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
public class UpdateDialogSoftwareLicenseKey implements Initializable {

    @FXML
    private JFXButton CloseButton;

    @FXML
    private JFXButton UpdateButton;

    @FXML
    private TextArea licenseKeyTextArea;
    @Setter
    private Stage stage;
    @FXML
    private ComboBox<String> statusComboBox;

    private SoftwareLicenseKeyDTO softwareLicenseKeyToUpdate;

    @Autowired
    private SoftwareLicenseKeyService softwareLicenseKeyService;

    @Setter
    private Runnable afterUpdate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UpdateButton.setOnAction(this::updateSoftwareLicenseKey);
        CloseButton.setOnAction(this::closeDialog);
        loadStatusToComboBox();
    }

    public void setSoftwareLicenseKeyDTO(SoftwareLicenseKeyDTO softwareLicenseKeyDTO) {
        this.softwareLicenseKeyToUpdate = softwareLicenseKeyDTO;
        licenseKeyTextArea.setText(softwareLicenseKeyDTO.getLicenseKey());
        statusComboBox.setValue(softwareLicenseKeyDTO.getStatus().name());
    }


    private void updateSoftwareLicenseKey(ActionEvent event) {
        softwareLicenseKeyToUpdate.setAccountId(Model.getInstance().getLoggedInUser().getAccountId());
        softwareLicenseKeyToUpdate.setLicenseKey(licenseKeyTextArea.getText());
        softwareLicenseKeyToUpdate.setPaymentStatus(softwareLicenseKeyToUpdate.getPaymentStatus());
        softwareLicenseKeyToUpdate.setStatus(Status.valueOf(statusComboBox.getValue()));
        softwareLicenseKeyToUpdate.setProductId(softwareLicenseKeyToUpdate.getProductId());

        try {
            softwareLicenseKeyService.updateSoftwareLicenseKey(softwareLicenseKeyToUpdate);
            clearFields();
            if (afterUpdate != null) afterUpdate.run();
            Model.getInstance().getViewFactory().closeStage(stage);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error while updating software account");
        }
    }

    private void clearFields() {
        licenseKeyTextArea.clear();
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
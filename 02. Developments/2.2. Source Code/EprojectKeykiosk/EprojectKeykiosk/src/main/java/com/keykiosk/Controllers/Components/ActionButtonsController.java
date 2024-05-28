package com.keykiosk.Controllers.Components;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ActionButtonsController {

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

}

package com.keykiosk;

import com.keykiosk.Models.DTO.OrderDTO;
import com.keykiosk.Models.EnumType.OrderStatus;
import com.keykiosk.Models.EnumType.PaymentMethod;
import com.keykiosk.Models.Model;
import com.keykiosk.Services.OrderService;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;

@SpringBootApplication
public class MainApp extends Application {
    @Getter
    private static ConfigurableApplicationContext applicationContext;

    @Override
    public void start(Stage stage) throws Exception {
        Model.getInstance().getViewFactory().setCurrentStage(stage);
        Model.getInstance().getViewFactory().showLoginWindow();
    }

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }

    @Override
    public void init() {
        applicationContext = SpringApplication.run(MainApp.class);
    }
}

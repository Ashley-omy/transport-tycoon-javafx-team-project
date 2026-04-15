/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author lenovo
 */
import common.Money;
import controller.ActionResult;
import controller.TimeController;
import controller.TimeSpeed;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import javafx.scene.paint.Color;

public class HUDView extends HBox {

    private Label moneyLabel;
    private Label timeLabel;
    private Label speedLabel;
    private Label uiStateLabel;
    private UIState uiState;
    private Button roadBtn;
    private Button stopBtn;
    private Button garageBtn;
    private Button routeBtn;
    private Button deconstructBtn;
    private Button pauseBtn;
    private Button normalSpeedBtn;
    private Button fastSpeedBtn;
    private Button veryFastSpeedBtn;
    private Label buildResultLabel;
    String lastMessage;

    public HUDView(UIState uiState, TimeController timeController) {
        moneyLabel = new Label("Money: 0");
        timeLabel = new Label("Time: 0");
        speedLabel = new Label("Speed: NORMAL");
        this.uiState = uiState;
        roadBtn = new Button("Road");
        stopBtn = new Button("Stop");
        garageBtn = new Button("Garage");
        deconstructBtn = new Button("Deconstruct");
        routeBtn = new Button("Place Route");
        pauseBtn = new Button("Pause");
        normalSpeedBtn = new Button("1x");
        fastSpeedBtn = new Button("2x");
        veryFastSpeedBtn = new Button("4x");
        uiStateLabel = new Label("UI State");
        buildResultLabel = new Label();

        disableFocusRing(
                roadBtn, stopBtn, garageBtn, deconstructBtn, routeBtn,
                pauseBtn, normalSpeedBtn, fastSpeedBtn, veryFastSpeedBtn
        );

        // Bind actions: clicking the same build button again resets to NONE mode.
        roadBtn.setOnAction(e -> toggleBuildMode(BuildMode.ROAD));
        stopBtn.setOnAction(e -> toggleBuildMode(BuildMode.STOP));
        garageBtn.setOnAction(e -> toggleBuildMode(BuildMode.GARAGE));
        deconstructBtn.setOnAction(e -> toggleBuildMode(BuildMode.DECONSTRUCT));
        pauseBtn.setOnAction(e -> timeController.setSpeed(TimeSpeed.PAUSE));
        normalSpeedBtn.setOnAction(e -> timeController.setSpeed(TimeSpeed.NORMAL));
        fastSpeedBtn.setOnAction(e -> timeController.setSpeed(TimeSpeed.FAST));
        veryFastSpeedBtn.setOnAction(e -> timeController.setSpeed(TimeSpeed.VERY_FAST));
        routeBtn.setOnAction(e -> {
            if (uiState.getBuildMode() == BuildMode.ROUTE) {
                uiState.requestRoutePlacement();
                uiState.setBuildMode(BuildMode.NONE);
            } else {
                uiState.setBuildMode(BuildMode.ROUTE);
            }
        });

        this.getChildren().addAll(
                moneyLabel,
                timeLabel,
                speedLabel,
                uiStateLabel,
                stopBtn,
                garageBtn,
                roadBtn,
                deconstructBtn,
                routeBtn,
                pauseBtn,
                normalSpeedBtn,
                fastSpeedBtn,
                veryFastSpeedBtn,
                buildResultLabel
        );

    }

    private void toggleBuildMode(BuildMode mode) {
        if (uiState.getBuildMode() == mode) {
            uiState.setBuildMode(BuildMode.NONE);
            return;
        }
        uiState.setBuildMode(mode);
    }

    private void disableFocusRing(Button... buttons) {
        for (Button button : buttons) {
            button.setFocusTraversable(false);
            button.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        }
    }

    public void displayBuildResult(ActionResult result){
        if(result == null){ return; }

        lastMessage = result.getMessage();

        buildResultLabel.setTextFill(
                result.isSuccess() ? Color.GREEN : Color.RED
        );
        buildResultLabel.setText(lastMessage);
    }

    public void render(Money cash, String time, TimeSpeed speed) {

            speedLabel.setText(" / Speed: " + speed);
            moneyLabel.setText(" / Money: " + (cash));
            timeLabel.setText(" / Time: " + time);
            uiStateLabel.setText(" / UI State: " + uiState.getBuildMode());
        }
    }

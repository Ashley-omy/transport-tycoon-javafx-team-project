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
        // Bind actions
        roadBtn.setOnAction(e -> uiState.setBuildMode(BuildMode.ROAD));
        stopBtn.setOnAction(e -> uiState.setBuildMode(BuildMode.STOP));
        garageBtn.setOnAction(e -> uiState.setBuildMode(BuildMode.GARAGE));
        deconstructBtn.setOnAction(e -> uiState.setBuildMode(BuildMode.DECONSTRUCT));
        pauseBtn.setOnAction(e -> timeController.setSpeed(TimeSpeed.PAUSE));
        normalSpeedBtn.setOnAction(e -> timeController.setSpeed(TimeSpeed.NORMAL));
        fastSpeedBtn.setOnAction(e -> timeController.setSpeed(TimeSpeed.FAST));
        veryFastSpeedBtn.setOnAction(e -> timeController.setSpeed(TimeSpeed.VERY_FAST));
        routeBtn.setOnAction(e -> {
            if (uiState.getBuildMode() == BuildMode.ROUTE) {
                uiState.requestRoutePlacement();
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
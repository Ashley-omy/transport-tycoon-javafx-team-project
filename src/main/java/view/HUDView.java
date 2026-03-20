/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author lenovo
 */
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class HUDView extends HBox {

    private Label moneyLabel;
    private Label timeLabel;
    private UIState uiState;
    private Button roadBtn;
    private Button stopBtn;
    private Button garageBtn;

    public HUDView(UIState uiState) {
        moneyLabel = new Label("Money: 0");
        timeLabel = new Label("Time: 0");
        this.uiState = uiState;
        roadBtn = new Button("Road");
        stopBtn = new Button("Stop");
        garageBtn = new Button("Garage");

        // Bind actions
        roadBtn.setOnAction(e -> uiState.setBuildMode(BuildMode.ROAD));
        stopBtn.setOnAction(e -> uiState.setBuildMode(BuildMode.STOP));
        garageBtn.setOnAction(e -> uiState.setBuildMode(BuildMode.GARAGE));

        this.getChildren().addAll(moneyLabel, timeLabel,roadBtn, stopBtn, garageBtn);

    }

    public void render() {
        // Later: bind to model
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */
import common.Money;
import controller.TimeSpeed;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

public class HUDView extends HBox {
    private static final String HUD_TEXT_STYLE = "-fx-font-size: 16px; -fx-font-weight: bold;";

    private Label moneyLabel;
    private Label timeLabel;
    private Label speedLabel;
    private Label uiStateLabel;
    private UIState uiState;

    public HUDView(UIState uiState) {
        moneyLabel = new Label("Money: 0");
        timeLabel = new Label("Time: 0");
        speedLabel = new Label("Speed: NORMAL");
        this.uiState = uiState;
        uiStateLabel = new Label("UI State");

        setPadding(new Insets(8, 0, 8, 16));
        setSpacing(14);

        moneyLabel.setStyle(HUD_TEXT_STYLE);
        timeLabel.setStyle(HUD_TEXT_STYLE);
        speedLabel.setStyle(HUD_TEXT_STYLE);
        uiStateLabel.setStyle(HUD_TEXT_STYLE);

        this.getChildren().addAll(
                moneyLabel,
                timeLabel,
                speedLabel,
                uiStateLabel
        );

    }

    public void render(Money cash, String time, TimeSpeed speed) {

            speedLabel.setText("  Speed: " + speed);
            moneyLabel.setText("  Money: " + (cash));
            timeLabel.setText("  Time: " + time);
            uiStateLabel.setText("  UI State: " + uiState.getBuildMode());
        }
    }

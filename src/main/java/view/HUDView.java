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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;

public class HUDView extends HBox {
    private static final String HUD_TEXT_STYLE = "-fx-font-size: 16px; -fx-font-weight: bold;";
    private static final String EARN_TEXT_STYLE = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6dff6f;";
    private static final String COST_TEXT_STYLE = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF4D4D;";
    private static final long MESSAGE_DURATION_NANOS = 2_000_000_000L;

    private Label moneyLabel;
    private Label timeLabel;
    private Label speedLabel;
    private Label uiStateLabel;
    private Label earnLabel;
    private Label costLabel;
    private final Region spacer;
    private UIState uiState;
    private long earnMessageHideAtNanos;
    private long costMessageHideAtNanos;

    public HUDView(UIState uiState) {
        moneyLabel = new Label("Money: 0");
        timeLabel = new Label("Time: 0");
        speedLabel = new Label("Speed: NORMAL");
        this.uiState = uiState;
        uiStateLabel = new Label("UI State");
        earnLabel = new Label("");
        costLabel = new Label("");
        spacer = new Region();

        setPadding(new Insets(8, 0, 8, 16));
        setSpacing(14);
        setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        moneyLabel.setStyle(HUD_TEXT_STYLE);
        timeLabel.setStyle(HUD_TEXT_STYLE);
        speedLabel.setStyle(HUD_TEXT_STYLE);
        uiStateLabel.setStyle(HUD_TEXT_STYLE);
        earnLabel.setStyle(EARN_TEXT_STYLE);
        costLabel.setStyle(COST_TEXT_STYLE);
        earnLabel.setVisible(false);
        costLabel.setVisible(false);

        this.getChildren().addAll(
                moneyLabel,
                timeLabel,
                speedLabel,
                uiStateLabel,
                earnLabel,
                costLabel,
                spacer
        );

    }

    public void showEarnMessage(Money amount) {
        if (amount == null || !amount.isPositive()) {
            return;
        }
        earnLabel.setText("earn +" + amount.amount() + " coins");
        earnLabel.setVisible(true);
        earnMessageHideAtNanos = System.nanoTime() + MESSAGE_DURATION_NANOS;
    }

    public void showEarnMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        earnLabel.setText(message);
        earnLabel.setVisible(true);
        earnMessageHideAtNanos = System.nanoTime() + MESSAGE_DURATION_NANOS;
    }

    public void showCostMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        costLabel.setText(message);
        costLabel.setVisible(true);
        costMessageHideAtNanos = System.nanoTime() + MESSAGE_DURATION_NANOS;
    }

    public void render(Money cash, String time, TimeSpeed speed) {

            speedLabel.setText("  Speed: " + speed);
            moneyLabel.setText("  Money: " + (cash));
            timeLabel.setText("  Time: " + time);
            uiStateLabel.setText("  UI State: " + uiState.getBuildMode());
            if (earnLabel.isVisible() && System.nanoTime() >= earnMessageHideAtNanos) {
                earnLabel.setVisible(false);
                earnLabel.setText("");
            }
            if (costLabel.isVisible() && System.nanoTime() >= costMessageHideAtNanos) {
                costLabel.setVisible(false);
                costLabel.setText("");
            }
        }
    }

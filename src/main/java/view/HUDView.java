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
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;

public class HUDView extends HBox {
    private static final String HUD_PANEL_STYLE =
            "-fx-background-color: #ffd669;" +
            "-fx-background-radius: 10;";
    private static final String HUD_KEY_TEXT_STYLE_NORMAL = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #8d7750;";
    private static final String HUD_VALUE_TEXT_STYLE_NORMAL = "-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #4e3b1f;";
    private static final String EARN_TEXT_STYLE_NORMAL = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6dff6f;";
    private static final String COST_TEXT_STYLE_NORMAL = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF4D4D;";
    private static final String HUD_KEY_TEXT_STYLE_FULLSCREEN = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8d7750;";
    private static final String HUD_VALUE_TEXT_STYLE_FULLSCREEN = "-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #4e3b1f;";
    private static final String EARN_TEXT_STYLE_FULLSCREEN = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6dff6f;";
    private static final String COST_TEXT_STYLE_FULLSCREEN = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FF4D4D;";
    private static final long MESSAGE_DURATION_NANOS = 2_000_000_000L;

    private final Label moneyValueLabel;
    private final Label timeValueLabel;
    private final Label speedValueLabel;
    private final Label uiStateValueLabel;
    private final Label earnLabel;
    private final Label costLabel;
    private final Label moneyKeyLabel;
    private final Label timeKeyLabel;
    private final Label speedKeyLabel;
    private final Label uiStateKeyLabel;
    private final UIState uiState;
    private Scene boundScene;
    private Stage boundStage;
    private final ChangeListener<Boolean> fullScreenListener = (obs, oldVal, isFullScreen) -> applyFontStyles(isFullScreen);
    private final ChangeListener<Window> windowListener = (obs, oldWindow, newWindow) -> bindToWindow(newWindow);
    private final ChangeListener<Scene> sceneListener = (obs, oldScene, newScene) -> bindToScene(newScene);
    private long earnMessageHideAtNanos;
    private long costMessageHideAtNanos;

    public HUDView(UIState uiState) {
        moneyValueLabel = new Label("0");
        timeValueLabel = new Label("0");
        speedValueLabel = new Label("NORMAL");
        this.uiState = uiState;
        uiStateValueLabel = new Label("NONE");
        earnLabel = new Label("");
        costLabel = new Label("");
        moneyKeyLabel = new Label("Money:");
        timeKeyLabel = new Label("Time:");
        speedKeyLabel = new Label("Speed:");
        uiStateKeyLabel = new Label("UI State:");

        setPadding(new Insets(8, 0, 8, 16));
        setSpacing(14);
        setAlignment(Pos.CENTER);
        setMaxWidth(Double.MAX_VALUE);
        setStyle(HUD_PANEL_STYLE);

        moneyValueLabel.setStyle(HUD_VALUE_TEXT_STYLE_NORMAL);
        timeValueLabel.setStyle(HUD_VALUE_TEXT_STYLE_NORMAL);
        speedValueLabel.setStyle(HUD_VALUE_TEXT_STYLE_NORMAL);
        uiStateValueLabel.setStyle(HUD_VALUE_TEXT_STYLE_NORMAL);
        earnLabel.setStyle(EARN_TEXT_STYLE_NORMAL);
        costLabel.setStyle(COST_TEXT_STYLE_NORMAL);
        earnLabel.setVisible(false);
        costLabel.setVisible(false);

        HBox moneyRow = createMetricRow(moneyKeyLabel, moneyValueLabel);
        HBox timeRow = createMetricRow(timeKeyLabel, timeValueLabel);
        HBox speedRow = createMetricRow(speedKeyLabel, speedValueLabel);
        HBox uiStateRow = createMetricRow(uiStateKeyLabel, uiStateValueLabel);

        this.getChildren().addAll(
                moneyRow,
                timeRow,
                speedRow,
                uiStateRow,
                earnLabel,
                costLabel
        );

        sceneProperty().addListener(sceneListener);
        bindToScene(getScene());
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
        speedValueLabel.setText(String.valueOf(speed));
        moneyValueLabel.setText(String.valueOf(cash));
        timeValueLabel.setText(time);
        uiStateValueLabel.setText(String.valueOf(uiState.getBuildMode()));
        if (earnLabel.isVisible() && System.nanoTime() >= earnMessageHideAtNanos) {
            earnLabel.setVisible(false);
            earnLabel.setText("");
        }
        if (costLabel.isVisible() && System.nanoTime() >= costMessageHideAtNanos) {
            costLabel.setVisible(false);
            costLabel.setText("");
        }
    }

    private HBox createMetricRow(Label keyLabel, Label valueLabel) {
        keyLabel.setStyle(HUD_KEY_TEXT_STYLE_NORMAL);
        HBox row = new HBox(4, keyLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void bindToScene(Scene scene) {
        if (boundScene != null) {
            boundScene.windowProperty().removeListener(windowListener);
        }
        boundScene = scene;
        if (boundScene != null) {
            boundScene.windowProperty().addListener(windowListener);
        }

        Window window = boundScene == null ? null : boundScene.getWindow();
        bindToWindow(window);
    }

    private void bindToWindow(Window window) {
        Stage stage = window instanceof Stage ? (Stage) window : null;
        if (boundStage == stage) {
            return;
        }
        if (boundStage != null) {
            boundStage.fullScreenProperty().removeListener(fullScreenListener);
        }
        boundStage = stage;
        if (boundStage != null) {
            boundStage.fullScreenProperty().addListener(fullScreenListener);
            applyFontStyles(boundStage.isFullScreen());
        } else {
            applyFontStyles(false);
        }
    }

    private void applyFontStyles(boolean fullScreen) {
        String keyStyle = fullScreen ? HUD_KEY_TEXT_STYLE_FULLSCREEN : HUD_KEY_TEXT_STYLE_NORMAL;
        String valueStyle = fullScreen ? HUD_VALUE_TEXT_STYLE_FULLSCREEN : HUD_VALUE_TEXT_STYLE_NORMAL;
        String earnStyle = fullScreen ? EARN_TEXT_STYLE_FULLSCREEN : EARN_TEXT_STYLE_NORMAL;
        String costStyle = fullScreen ? COST_TEXT_STYLE_FULLSCREEN : COST_TEXT_STYLE_NORMAL;

        moneyKeyLabel.setStyle(keyStyle);
        timeKeyLabel.setStyle(keyStyle);
        speedKeyLabel.setStyle(keyStyle);
        uiStateKeyLabel.setStyle(keyStyle);

        moneyValueLabel.setStyle(valueStyle);
        timeValueLabel.setStyle(valueStyle);
        speedValueLabel.setStyle(valueStyle);
        uiStateValueLabel.setStyle(valueStyle);
        earnLabel.setStyle(earnStyle);
        costLabel.setStyle(costStyle);
    }
}

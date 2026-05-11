package view;

import controller.ActionResult;
import controller.TimeController;
import controller.TimeSpeed;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ControlPanes {
    private static final double BASE_BUTTON_WIDTH = 72.0;
    private static final double BASE_BUTTON_HEIGHT = 28.0;
    private static final double BUILD_BUTTON_WIDTH = 150.0;
    private static final double BUILD_BUTTON_HEIGHT = 73.0;
    private static final double BOTTOM_BUTTON_HEIGHT = 56.0;
    private static final double SPEED_BUTTON_WIDTH = BASE_BUTTON_WIDTH;
    private static final double SPEED_BUTTON_HEIGHT = BASE_BUTTON_HEIGHT;
    private static final String BUILD_RESULT_STYLE = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5d4423;";
    private static final PseudoClass SELECTED_BUILD = PseudoClass.getPseudoClass("selected-build");
    private static final String BUILD_PANE_STYLE =
            "-fx-background-color: #ffd669; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 8 10 10 10;";
    private static final String SPEED_PANE_STYLE =
            "-fx-background-color: #ffd669; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 6 8 6 8;";

    private static final String BASE_BUTTON_STYLE =
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent; " +
            "-fx-background-color: linear-gradient(to bottom, #73c0ff, #2d8fe9); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 14; " +
            "-fx-border-radius: 14; " +
            "-fx-border-color: #cfe9ff; " +
            "-fx-border-width: 1.4; " +
            "-fx-effect: dropshadow(gaussian, rgba(32, 118, 204, 0.45), 8, 0.25, 0, 2);";
    private static final String ACTIVE_SPEED_STYLE =
            BASE_BUTTON_STYLE +
                    "-fx-background-color: linear-gradient(to bottom, #ffd18a, #f0942f); " +
                    "-fx-text-fill: #4d2a08; " +
                    "-fx-border-color: #ffe0b6; " +
                    "-fx-border-width: 1.8;";
    private static final String POP_UI_STYLESHEET_PATH = "/styles/pop-ui.css";

    private final UIState uiState;
    private final TimeController timeController;
    private final BooleanSupplier onSaveRequested;
    private final Runnable onExitRequested;

    private final HBox speedPane = new HBox(6);
    private final VBox buildPane = new VBox(8);

    private final Button roadBtn = new Button("Road");
    private final Button bridgeBtn = new Button("Bridge");
    private final Button stopBtn = new Button("Stop");
    private final Button garageBtn = new Button("Garage");
    private final Button deconstructBtn = new Button("Deconstruct");
    private final Button routeBtn = new Button("Place Route");
    private final Button pauseBtn = new Button("Pause");
    private final Button saveBtn = new Button("Save");
    private final Button exitBtn = new Button("Exit");
    private final Button normalSpeedBtn = new Button("1x");
    private final Button fastSpeedBtn = new Button("2x");
    private final Button veryFastSpeedBtn = new Button("4x");
    private final Label buildResultLabel = new Label();
    private final Region buildPaneSpacer = new Region();

    public ControlPanes(UIState uiState, TimeController timeController, BooleanSupplier onSaveRequested, Runnable onExitRequested) {
        this.uiState = uiState;
        this.timeController = timeController;
        this.onSaveRequested = onSaveRequested == null ? () -> false : onSaveRequested;
        this.onExitRequested = onExitRequested == null ? () -> { } : onExitRequested;

        speedPane.setAlignment(Pos.CENTER_RIGHT);
        speedPane.setFillHeight(false);
        speedPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        speedPane.setStyle(SPEED_PANE_STYLE);

        buildPane.setAlignment(Pos.TOP_LEFT);
        buildPane.setFillWidth(false);
        buildPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        buildPane.setStyle(BUILD_PANE_STYLE);

        configureBuildButton(roadBtn);
        configureBuildButton(bridgeBtn);
        configureBuildButton(stopBtn);
        configureBuildButton(garageBtn);
        configureBuildButton(deconstructBtn);
        configureBuildButton(routeBtn);
        configureBuildButton(saveBtn);
        configureBuildButton(exitBtn);
        configureBottomButton(saveBtn);
        configureBottomButton(exitBtn);
        configureSpeedButton(pauseBtn);
        configureSpeedButton(normalSpeedBtn);
        configureSpeedButton(fastSpeedBtn);
        configureSpeedButton(veryFastSpeedBtn);

        roadBtn.getStyleClass().add("btn-road");
        bridgeBtn.getStyleClass().add("btn-bridge");
        stopBtn.getStyleClass().add("btn-stop");
        garageBtn.getStyleClass().add("btn-garage");
        deconstructBtn.getStyleClass().add("btn-deconstruct");
        routeBtn.getStyleClass().add("btn-route");
        saveBtn.getStyleClass().add("btn-save");
        exitBtn.getStyleClass().add("btn-exit");

        roadBtn.setOnAction(e -> toggleBuildMode(BuildMode.ROAD));
        bridgeBtn.setOnAction(e -> {
            if (uiState.getBuildMode() == BuildMode.BRIDGE) {
                uiState.requestBridgeTypeSelection();
            } else {
                uiState.setBuildMode(BuildMode.BRIDGE);
            }
            refreshBuildButtonStyles();
        });
        stopBtn.setOnAction(e -> toggleBuildMode(BuildMode.STOP));
        garageBtn.setOnAction(e -> toggleBuildMode(BuildMode.GARAGE));
        deconstructBtn.setOnAction(e -> toggleBuildMode(BuildMode.DECONSTRUCT));
        routeBtn.setOnAction(e -> {
            if (uiState.getBuildMode() == BuildMode.ROUTE) {
                uiState.setBuildMode(BuildMode.NONE);
                uiState.requestRoutePlacement();
            } else {
                uiState.setBuildMode(BuildMode.ROUTE);
            }
            refreshBuildButtonStyles();
        });

        pauseBtn.setOnAction(e -> {
            timeController.setSpeed(TimeSpeed.PAUSE);
            refreshSpeedButtonStyles();
        });
        normalSpeedBtn.setOnAction(e -> {
            timeController.setSpeed(TimeSpeed.NORMAL);
            refreshSpeedButtonStyles();
        });
        fastSpeedBtn.setOnAction(e -> {
            timeController.setSpeed(TimeSpeed.FAST);
            refreshSpeedButtonStyles();
        });
        veryFastSpeedBtn.setOnAction(e -> {
            timeController.setSpeed(TimeSpeed.VERY_FAST);
            refreshSpeedButtonStyles();
        });
        saveBtn.setOnAction(e -> onSaveRequested.getAsBoolean());
        exitBtn.setOnAction(e -> handleExitRequested());

        speedPane.getChildren().addAll(
                pauseBtn,
                normalSpeedBtn,
                fastSpeedBtn,
                veryFastSpeedBtn
        );

        buildResultLabel.setWrapText(true);
        buildResultLabel.setMinWidth(BUILD_BUTTON_WIDTH);
        buildResultLabel.setPrefWidth(BUILD_BUTTON_WIDTH);
        buildResultLabel.setMaxWidth(BUILD_BUTTON_WIDTH);
        buildResultLabel.setStyle(BUILD_RESULT_STYLE);
        VBox.setVgrow(buildPaneSpacer, Priority.ALWAYS);
        VBox.setMargin(exitBtn, new Insets(0, 0, 15, 0));

        buildPane.getChildren().addAll(
                roadBtn,
                bridgeBtn,
                stopBtn,
                garageBtn,
                deconstructBtn,
                routeBtn,
                buildResultLabel,
                buildPaneSpacer,
                saveBtn,
                exitBtn
        );

        refreshBuildButtonStyles();
        refreshSpeedButtonStyles();
    }

    public HBox getSpeedPane() {
        return speedPane;
    }

    public VBox getBuildPane() {
        return buildPane;
    }

    public void render() {
        refreshBuildButtonStyles();
        refreshSpeedButtonStyles();
    }

    public void displayBuildResult(ActionResult result) {
        if (result == null) {
            return;
        }
        buildResultLabel.setTextFill(result.isSuccess() ? Color.GREEN : Color.RED);
        buildResultLabel.setText(result.getMessage());
    }

    private void configureBuildButton(Button button) {
        button.setFocusTraversable(false);
        button.setMinSize(BUILD_BUTTON_WIDTH, BUILD_BUTTON_HEIGHT);
        button.setPrefSize(BUILD_BUTTON_WIDTH, BUILD_BUTTON_HEIGHT);
        button.setMaxSize(BUILD_BUTTON_WIDTH, BUILD_BUTTON_HEIGHT);
        button.getStyleClass().add("build-button");
    }

    private void configureSpeedButton(Button button) {
        button.setFocusTraversable(false);
        button.setMinSize(SPEED_BUTTON_WIDTH, SPEED_BUTTON_HEIGHT);
        button.setPrefSize(SPEED_BUTTON_WIDTH, SPEED_BUTTON_HEIGHT);
        button.setMaxSize(SPEED_BUTTON_WIDTH, SPEED_BUTTON_HEIGHT);
        button.setStyle(BASE_BUTTON_STYLE);
    }

    private void configureBottomButton(Button button) {
        button.setMinSize(BUILD_BUTTON_WIDTH, BOTTOM_BUTTON_HEIGHT);
        button.setPrefSize(BUILD_BUTTON_WIDTH, BOTTOM_BUTTON_HEIGHT);
        button.setMaxSize(BUILD_BUTTON_WIDTH, BOTTOM_BUTTON_HEIGHT);
    }

    private void toggleBuildMode(BuildMode mode) {
        if (uiState.getBuildMode() == mode) {
            uiState.setBuildMode(BuildMode.NONE);
        } else {
            uiState.setBuildMode(mode);
        }
        refreshBuildButtonStyles();
    }

    private void refreshBuildButtonStyles() {
        applyBuildSelectionStyle(roadBtn, BuildMode.ROAD);
        applyBuildSelectionStyle(bridgeBtn, BuildMode.BRIDGE);
        applyBuildSelectionStyle(stopBtn, BuildMode.STOP);
        applyBuildSelectionStyle(garageBtn, BuildMode.GARAGE);
        applyBuildSelectionStyle(deconstructBtn, BuildMode.DECONSTRUCT);
        applyBuildSelectionStyle(routeBtn, BuildMode.ROUTE);
    }

    private void refreshSpeedButtonStyles() {
        TimeSpeed speed = timeController.getSpeed();
        pauseBtn.setStyle(speed == TimeSpeed.PAUSE ? ACTIVE_SPEED_STYLE : BASE_BUTTON_STYLE);
        normalSpeedBtn.setStyle(speed == TimeSpeed.NORMAL ? ACTIVE_SPEED_STYLE : BASE_BUTTON_STYLE);
        fastSpeedBtn.setStyle(speed == TimeSpeed.FAST ? ACTIVE_SPEED_STYLE : BASE_BUTTON_STYLE);
        veryFastSpeedBtn.setStyle(speed == TimeSpeed.VERY_FAST ? ACTIVE_SPEED_STYLE : BASE_BUTTON_STYLE);
    }

    private void applyBuildSelectionStyle(Button button, BuildMode mode) {
        boolean selected = uiState.getBuildMode() == mode;
        button.pseudoClassStateChanged(SELECTED_BUILD, selected);
    }

    private void handleExitRequested() {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Exit");
        dialog.setHeaderText(null);
        dialog.setContentText("Do you want to save current status before exiting?");
        dialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        if (buildPane.getScene() != null && buildPane.getScene().getWindow() != null) {
            dialog.initOwner(buildPane.getScene().getWindow());
        }
        styleDialog(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        if (result.get() == ButtonType.YES) {
            if (onSaveRequested.getAsBoolean()) {
                onExitRequested.run();
            }
            return;
        }

        if (result.get() == ButtonType.NO) {
            onExitRequested.run();
        }
    }

    private void styleDialog(Dialog<?> dialog) {
        if (dialog == null || dialog.getDialogPane() == null) {
            return;
        }

        var stylesheetUrl = ControlPanes.class.getResource(POP_UI_STYLESHEET_PATH);
        if (stylesheetUrl != null) {
            String stylesheet = stylesheetUrl.toExternalForm();
            if (!dialog.getDialogPane().getStylesheets().contains(stylesheet)) {
                dialog.getDialogPane().getStylesheets().add(stylesheet);
            }
        }
        if (!dialog.getDialogPane().getStyleClass().contains("pop-dialog")) {
            dialog.getDialogPane().getStyleClass().add("pop-dialog");
        }
    }
}

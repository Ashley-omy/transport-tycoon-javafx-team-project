package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameOverPane extends StackPane {
    public GameOverPane(Runnable onRestartRequested, Runnable onLeaveRequested) {
        Runnable restartAction = onRestartRequested == null ? () -> { } : onRestartRequested;
        Runnable leaveAction = onLeaveRequested == null ? () -> { } : onLeaveRequested;

        Label title = new Label("BANKRUPT ~GAME OVER~");
        title.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #ff6a6a;");

        Button restartButton = new Button("Restart");
        Button leaveButton = new Button("Leave the game");
        restartButton.setMinSize(180, 48);
        restartButton.setPrefSize(180, 48);
        leaveButton.setMinSize(180, 48);
        leaveButton.setPrefSize(180, 48);
        restartButton.setOnAction(e -> restartAction.run());
        leaveButton.setOnAction(e -> leaveAction.run());

        HBox buttonRow = new HBox(12, restartButton, leaveButton);
        buttonRow.setAlignment(Pos.CENTER);

        VBox panel = new VBox(24, title, buttonRow);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(28));
        panel.setStyle(
                "-fx-background-color: rgba(20, 20, 20, 0.94);"
                        + "-fx-border-color: #ff6a6a;"
                        + "-fx-border-width: 2;"
        );

        getChildren().add(panel);
        setVisible(false);
        setManaged(false);
        setPickOnBounds(true);
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }
}

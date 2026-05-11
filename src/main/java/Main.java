import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import view.GameWindow;
import view.StartMenuPane;

import model.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class Main extends Application {
    private static final int WORLD_WIDTH = 100;
    private static final int WORLD_HEIGHT = 100;
    private static final int SCENE_WIDTH = 1180;
    private static final int SCENE_HEIGHT = 750;
    private static final DateTimeFormatter SAVE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String POP_UI_STYLESHEET_PATH = "/styles/pop-ui.css";

    private final SaveService saveService = new SaveService();
    private final Region startMenuDimmer = new Region();
    private GameWindow currentGameWindow;
    private Game currentGame;
    private StackPane rootContainer;
    private Parent baseContent;
    private StartMenuPane startMenuPane;
    private boolean startMenuVisible;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Transport Tycoon");
        restartGame(stage);
        showStartMenu(stage);
        stage.setFullScreen(true);
        stage.show();
    }

    private void showStartMenu(Stage stage) {
        startMenuPane = new StartMenuPane(
                hasSavedGames(),
                () -> {
                    restartGame(stage);
                    hideStartMenu();
                },
                () -> chooseAndLoadGame(stage)
        );
        startMenuDimmer.setStyle("-fx-background-color: rgba(255, 236, 181, 0.45);");
        startMenuDimmer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        startMenuVisible = true;
        updateRoot(stage);
        Platform.runLater(startMenuPane::requestFocus);
    }

    private void hideStartMenu() {
        startMenuVisible = false;
        updateRoot(null);
        if (currentGameWindow != null) {
            Platform.runLater(currentGameWindow::requestFocus);
        }
    }

    private void restartGame(Stage stage) {
        World world = new World(WORLD_WIDTH, WORLD_HEIGHT);
        Company company = new Company();
        Game game = new Game(world, company);
        loadGame(stage, game);
    }

    private void loadGame(Stage stage, Game game) {
        if (currentGameWindow != null) {
            currentGameWindow.dispose();
        }

        GameWindow nextWindow = new GameWindow(
                game,
                game.getWorld(),
                game.getCompany(),
                () -> restartGame(stage),
                Platform::exit,
                () -> saveCurrentGame(stage)
        );
        currentGameWindow = nextWindow;
        currentGame = game;

        baseContent = nextWindow;
        updateRoot(stage);
        Platform.runLater(nextWindow::requestFocus);
    }

    private boolean saveCurrentGame(Stage stage) {
        if (currentGame == null) {
            showError(stage, "Save failed", "No active game to save.");
            return false;
        }

        String defaultName = "save_" + LocalDateTime.now().format(SAVE_NAME_FORMAT);
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Save Game");
        dialog.setHeaderText(null);
        dialog.setContentText("Save name:");
        dialog.initOwner(stage);
        styleDialog(dialog);

        Optional<String> saveNameResult = dialog.showAndWait();
        if (saveNameResult.isEmpty()) {
            return false;
        }

        String saveName = saveNameResult.get();
        try {
            saveService.save(currentGame, saveName);
            showInfo(stage, "Game saved", "Saved as " + saveName.trim());
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            showError(stage, "Save failed", ex.getMessage());
            return false;
        }
    }

    private boolean hasSavedGames() {
        try {
            return !saveService.listSaves().isEmpty();
        } catch (IOException ex) {
            return false;
        }
    }

    private void chooseAndLoadGame(Stage stage) {
        List<String> saves;
        try {
            saves = saveService.listSaves();
        } catch (IOException ex) {
            showError(stage, "Load failed", ex.getMessage());
            return;
        }

        if (saves.isEmpty()) {
            showInfo(stage, "No saves", "No saved games were found.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(saves.get(0), saves);
        dialog.setTitle("Load Game");
        dialog.setHeaderText(null);
        dialog.setContentText("Saved game:");
        dialog.initOwner(stage);
        styleDialog(dialog);

        dialog.showAndWait().ifPresent(saveName -> {
            try {
                Game loadedGame = saveService.load(saveName);
                loadGame(stage, loadedGame);
                hideStartMenu();
            } catch (IOException | ClassNotFoundException | IllegalArgumentException ex) {
                showError(stage, "Load failed", ex.getMessage());
            }
        });
    }

    private void updateRoot(Stage stage) {
        if (stage != null && rootContainer == null) {
            rootContainer = new StackPane();
            Scene scene = new Scene(rootContainer, SCENE_WIDTH, SCENE_HEIGHT);
            String stylesheet = resolveStylesheet(POP_UI_STYLESHEET_PATH);
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet);
            }
            stage.setScene(scene);
        }
        if (rootContainer == null || baseContent == null) {
            return;
        }

        rootContainer.getChildren().setAll(baseContent);
        if (startMenuVisible && startMenuPane != null) {
            rootContainer.getChildren().add(startMenuDimmer);
            rootContainer.getChildren().add(startMenuPane);
            StackPane.setAlignment(startMenuPane, Pos.CENTER);
        }
    }

    private void showInfo(Stage stage, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(stage);
        styleDialog(alert);
        alert.showAndWait();
    }

    private void showError(Stage stage, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null || message.isBlank() ? "Unknown error" : message);
        alert.initOwner(stage);
        styleDialog(alert);
        alert.showAndWait();
    }

    private void styleDialog(Dialog<?> dialog) {
        if (dialog == null || dialog.getDialogPane() == null) {
            return;
        }

        String stylesheet = resolveStylesheet(POP_UI_STYLESHEET_PATH);
        if (stylesheet != null && !dialog.getDialogPane().getStylesheets().contains(stylesheet)) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
        }
        if (!dialog.getDialogPane().getStyleClass().contains("pop-dialog")) {
            dialog.getDialogPane().getStyleClass().add("pop-dialog");
        }
    }

    private String resolveStylesheet(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }

        var url = Main.class.getResource(resourcePath);
        return url == null ? null : url.toExternalForm();
    }

    public static void main(String[] args) {
        launch();
    }
}

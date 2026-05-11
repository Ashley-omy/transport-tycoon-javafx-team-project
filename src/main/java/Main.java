import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
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

public class Main extends Application {
    private static final int WORLD_WIDTH = 100;
    private static final int WORLD_HEIGHT = 100;
    private static final int SCENE_WIDTH = 1180;
    private static final int SCENE_HEIGHT = 750;
    private static final DateTimeFormatter SAVE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final SaveService saveService = new SaveService();
    private final Region startMenuDimmer = new Region();
    private GameWindow currentGameWindow;
    private Game currentGame;
    private String currentSaveName;
    private StackPane rootContainer;
    private Parent baseContent;
    private StartMenuPane startMenuPane;
    private boolean startMenuVisible;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Transport Tycoon");
        restartGame(stage);
        showStartMenu(stage);
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
        startMenuDimmer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");
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
        currentSaveName = null;
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

    private void saveCurrentGame(Stage stage) {
        if (currentGame == null) {
            showError(stage, "Save failed", "No active game to save.");
            return;
        }

        String defaultName = currentSaveName != null
                ? currentSaveName
                : "save_" + LocalDateTime.now().format(SAVE_NAME_FORMAT);
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Save Game");
        dialog.setHeaderText(null);
        dialog.setContentText("Save name (same name overwrites existing save):");
        dialog.initOwner(stage);

        dialog.showAndWait().ifPresent(saveName -> {
            try {
                saveService.save(currentGame, saveName);
                currentSaveName = saveName.trim();
                showInfo(stage, "Game saved", "Saved as " + saveName.trim()
                        + " at game time " + currentGame.getFormattedTime());
            } catch (IOException | IllegalArgumentException ex) {
                showError(stage, "Save failed", ex.getMessage());
            }
        });
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

        dialog.showAndWait().ifPresent(saveName -> {
            try {
                Game loadedGame = saveService.load(saveName);
                currentSaveName = saveName;
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
            stage.setScene(new Scene(rootContainer, SCENE_WIDTH, SCENE_HEIGHT));
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
        alert.showAndWait();
    }

    private void showError(Stage stage, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message == null || message.isBlank() ? "Unknown error" : message);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}

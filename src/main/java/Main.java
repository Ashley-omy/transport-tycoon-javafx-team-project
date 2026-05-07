import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import view.GameWindow;

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
    private GameWindow currentGameWindow;
    private Game currentGame;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Transport Tycoon");
        restartGame(stage);
        stage.show();
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
                () -> saveCurrentGame(stage),
                () -> chooseAndLoadGame(stage)
        );
        currentGameWindow = nextWindow;
        currentGame = game;

        if (stage.getScene() == null) {
            stage.setScene(new Scene(nextWindow, SCENE_WIDTH, SCENE_HEIGHT));
        } else {
            stage.getScene().setRoot(nextWindow);
        }
        Platform.runLater(nextWindow::requestFocus);
    }

    private void saveCurrentGame(Stage stage) {
        if (currentGame == null) {
            showError(stage, "Save failed", "No active game to save.");
            return;
        }

        String defaultName = "save_" + LocalDateTime.now().format(SAVE_NAME_FORMAT);
        TextInputDialog dialog = new TextInputDialog(defaultName);
        dialog.setTitle("Save Game");
        dialog.setHeaderText(null);
        dialog.setContentText("Save name:");
        dialog.initOwner(stage);

        dialog.showAndWait().ifPresent(saveName -> {
            try {
                saveService.save(currentGame, saveName);
                showInfo(stage, "Game saved", "Saved as " + saveName.trim());
            } catch (IOException | IllegalArgumentException ex) {
                showError(stage, "Save failed", ex.getMessage());
            }
        });
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
                loadGame(stage, loadedGame);
            } catch (IOException | ClassNotFoundException | IllegalArgumentException ex) {
                showError(stage, "Load failed", ex.getMessage());
            }
        });
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

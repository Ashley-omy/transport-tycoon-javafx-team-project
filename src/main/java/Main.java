import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.GameWindow;

import model.*;

public class Main extends Application {
    private static final int WORLD_WIDTH = 100;
    private static final int WORLD_HEIGHT = 100;
    private static final int SCENE_WIDTH = 1180;
    private static final int SCENE_HEIGHT = 750;

    private GameWindow currentGameWindow;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Transport Tycoon");
        restartGame(stage);
        stage.show();
    }

    private void restartGame(Stage stage) {
        if (currentGameWindow != null) {
            currentGameWindow.dispose();
        }

        World world = new World(WORLD_WIDTH, WORLD_HEIGHT);
        Company company = new Company();
        Game game = new Game(world, company);

        GameWindow nextWindow = new GameWindow(
                game,
                world,
                company,
                () -> restartGame(stage),
                Platform::exit
        );
        currentGameWindow = nextWindow;

        if (stage.getScene() == null) {
            stage.setScene(new Scene(nextWindow, SCENE_WIDTH, SCENE_HEIGHT));
        } else {
            stage.getScene().setRoot(nextWindow);
        }
        Platform.runLater(nextWindow::requestFocus);
    }

    public static void main(String[] args) {
        launch();
    }
}

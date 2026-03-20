import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.GameWindow;

import model.*;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Create model here (single instance)
        Game game = createGame();

        GameWindow root = new GameWindow(game);

        Scene scene = new Scene(root, 1200, 800);

        stage.setTitle("Transport Tycoon");
        stage.setScene(scene);
        stage.show();
    }
    private Game createGame() {

        World world = new World(100,100);
        Company company = new Company();

        return new Game(world, company);
    }

    public static void main(String[] args) {
        launch();
    }
}
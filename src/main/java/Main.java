import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.GameWindow;

import model.*;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        World world = new World(100,100);
        Company company = new Company();

        // Create model here (single instance)
        Game game = new Game(world, company);;

        GameWindow root = new GameWindow(game, world, company);

        Scene scene = new Scene(root, 1180, 750);

        stage.setTitle("Transport Tycoon");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
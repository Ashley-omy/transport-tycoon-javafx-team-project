import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.GameWindow;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        GameWindow root = new GameWindow();

        Scene scene = new Scene(root, 1200, 800);

        stage.setTitle("Transport Tycoon");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
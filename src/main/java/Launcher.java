// Manifest entry point: java -jar refuses to launch Main directly since it extends Application.
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}

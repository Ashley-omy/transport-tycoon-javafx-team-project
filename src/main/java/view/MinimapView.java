package view;
/**
 *
 * @author asuna
 */
import common.GridPos;
import common.Vec2;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import model.Bus;
import model.Company;
import model.Factory;
import model.GameMap;
import model.Mine;
import model.Tile;
import model.Truck;
import model.Vehicle;

public class MinimapView extends StackPane {
    private static final double WIDTH = 220;
    private static final double HEIGHT = 160;
    private static final double PADDING = 3;

    private final Canvas canvas;
    private final Camera camera;
    private GameMap map;
    private Company company;

    public MinimapView(Camera camera) {
        this.camera = camera;
        this.canvas = new Canvas(WIDTH, HEIGHT);

        getChildren().add(canvas);
        setPadding(new Insets(PADDING));
        setMaxSize(WIDTH + (PADDING * 2), HEIGHT + (PADDING * 2));
        setPickOnBounds(false);
        setStyle("-fx-background-color: rgba(20, 23, 30, 0.92);"
                + "-fx-background-radius: 5;"
                + "-fx-border-color: rgba(255,255,255,0.10);"
                + "-fx-border-width: 0.05;"
                + "-fx-border-radius: 5;");
    }

    public void setMap(GameMap map) {
        this.map = map;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void render() {
        if (map == null) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        double scaleX = canvas.getWidth() / map.getWidth();
        double scaleY = canvas.getHeight() / map.getHeight();

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(new GridPos(x, y));
                gc.setFill(getTileColor(tile));
                gc.fillRect(x * scaleX, y * scaleY, Math.ceil(scaleX), Math.ceil(scaleY));
            }
        }

        if (company != null) {
            for (Vehicle vehicle : company.getFleet()) {
                if (vehicle == null || vehicle.getWorldPos() == null) {
                    continue;
                }

                gc.setFill(vehicle instanceof Bus ? Color.DODGERBLUE
                        : vehicle instanceof Truck ? Color.DARKORANGE
                        : Color.LIGHTGRAY);
                gc.fillOval(
                        (vehicle.getWorldPos().x * scaleX) - 1.5,
                        (vehicle.getWorldPos().y * scaleY) - 1.5,
                        3,
                        3
                );
            }
        }

        drawViewport(gc, scaleX, scaleY);
    }

    public GridPos minimapToCameraTopLeft(Vec2 point) {
        if (map == null || point == null) {
            return null;
        }

        double scaleX = canvas.getWidth() / map.getWidth();
        double scaleY = canvas.getHeight() / map.getHeight();
        int clickedTileX = (int) Math.floor(point.x / scaleX);
        int clickedTileY = (int) Math.floor(point.y / scaleY);

        int visibleTilesX = Math.max(1, camera.getViewportW() / camera.getTileSize());
        int visibleTilesY = Math.max(1, camera.getViewportH() / camera.getTileSize());
        int targetX = clickedTileX - (visibleTilesX / 2);
        int targetY = clickedTileY - (visibleTilesY / 2);

        return new GridPos(targetX, targetY);
    }

    public GameMap getMap() {
        return map;
    }

    private void drawViewport(GraphicsContext gc, double scaleX, double scaleY) {
        GridPos topLeft = camera.getTopLeftTile();
        double viewportTilesW = Math.max(1, (double) camera.getViewportW() / camera.getTileSize());
        double viewportTilesH = Math.max(1, (double) camera.getViewportH() / camera.getTileSize());

        gc.setStroke(Color.rgb(255, 255, 255, 0.7));
        gc.setLineWidth(1);
        gc.strokeRect(
                topLeft.x * scaleX,
                topLeft.y * scaleY,
                viewportTilesW * scaleX,
                viewportTilesH * scaleY
        );
    }

    private Color getTileColor(Tile tile) {
        if (tile == null) {
            return Color.BLACK;
        }
        if (tile.isWater()) {
            return Color.rgb(78, 162, 219);
        }
        if (tile.isForest()) {
            return Color.rgb(48, 121, 63);
        }
        if (tile.getRoadPiece() != null) {
            return Color.rgb(92, 97, 107);
        }
        if (tile.getStop() != null) {
            return Color.rgb(244, 102, 93);
        }
        if (tile.getGarage() != null) {
            return Color.rgb(255, 175, 64);
        }
        if (tile.getEntity() instanceof Factory) {
            return Color.rgb(156, 113, 228);
        }
        if (tile.getEntity() instanceof Mine) {
            return Color.rgb(138, 87, 47);
        }
        if (tile.getEntity() != null) {
            return Color.rgb(212, 78, 78);
        }
        return Color.rgb(107, 176, 88);
    }
}

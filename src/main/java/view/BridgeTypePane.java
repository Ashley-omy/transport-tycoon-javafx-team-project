package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.BridgeSpec;
import model.BridgeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BridgeTypePane extends Stage {
    private static final String YAMABUKI = "#ffd669";
    private static final String BROWN_TEXT = "#5d4423";
    private static final String BUTTON_BG = "#d3a15a";
    private static final String BUTTON_BORDER = "#8a5d2d";
    private static final String PANE_FILL_STYLE = "-fx-background-color: " + YAMABUKI + ";";
    private static final String SCROLL_FILL_STYLE =
            "-fx-background: " + YAMABUKI + "; " +
            "-fx-background-color: " + YAMABUKI + "; " +
            "-fx-control-inner-background: " + YAMABUKI + ";";
    private static final String BUTTON_STYLE =
            "-fx-background-color: " + BUTTON_BG + "; " +
            "-fx-text-fill: " + BROWN_TEXT + "; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-border-color: " + BUTTON_BORDER + "; " +
            "-fx-border-width: 1.2;";
    private static final double BRIDGE_PREVIEW_WIDTH = 56;
    private static final double BRIDGE_PREVIEW_HEIGHT = 56;

    private final List<BridgeSpec> bridgeSpecs;
    private final Consumer<BridgeType> onPlace;
    private final VBox optionRows = new VBox(8);
    private final Button placeButton = new Button("place this bridge");
    private final Label titleLabel = new Label("Choose Bridge Type");

    private BridgeType selectedType;
    private final List<HBox> rowNodes = new ArrayList<>();

    public BridgeTypePane(List<BridgeSpec> bridgeSpecs, Consumer<BridgeType> onPlace) {
        this.bridgeSpecs = bridgeSpecs == null ? List.of() : List.copyOf(bridgeSpecs);
        this.onPlace = onPlace;

        setTitle("Choose Bridge Type");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + BROWN_TEXT + ";");

        optionRows.setPadding(new Insets(4));
        placeButton.setDisable(true);
        placeButton.setMinWidth(180);
        placeButton.setStyle(BUTTON_STYLE);
        placeButton.setOnAction(e -> placeSelectedBridge());

        ScrollPane scrollPane = new ScrollPane(optionRows);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(280);
        scrollPane.setStyle(SCROLL_FILL_STYLE);
        optionRows.setStyle(PANE_FILL_STYLE);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle(PANE_FILL_STYLE);
        root.setTop(titleLabel);
        root.setCenter(scrollPane);
        BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);

        HBox footer = new HBox(placeButton);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 0, 0, 0));
        root.setBottom(footer);

        Scene scene = new Scene(root, 460, 380);
        setScene(scene);
    }

    public void showForBridgeSelection(Window ownerWindow) {
        if (getOwner() == null && ownerWindow != null) {
            initOwner(ownerWindow);
        }
        selectedType = null;
        placeButton.setDisable(true);
        renderBridgeTypeRows();
        if (!isShowing()) {
            show();
        }
        toFront();
    }

    private void renderBridgeTypeRows() {
        optionRows.getChildren().clear();
        rowNodes.clear();

        for (BridgeSpec spec : bridgeSpecs) {
            HBox row = createRow(spec);
            rowNodes.add(row);
            optionRows.getChildren().add(row);
        }
    }

    private HBox createRow(BridgeSpec spec) {
        Node swatch = createBridgePreview(spec.getType());

        Label details = new Label(
                "Type: " + spec.getType()
                        + "\nmaxSpanTiles: " + spec.getMaxSpanTiles()
                        + "\ncost: " + spec.getCost()
                        + "\nspeedLimit: " + spec.getSpeedLimit()
        );
        details.setStyle("-fx-font-size: 14px; -fx-text-fill: " + BROWN_TEXT + ";");

        HBox row = new HBox(12, swatch, details);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        setRowSelectedStyle(row, false);
        row.setOnMouseClicked(e -> selectType(spec.getType(), row));
        return row;
    }

    private Node createBridgePreview(BridgeType type) {
        Image texture = BridgeVisuals.textureFor(type);
        if (texture != null && !texture.isError()) {
            ImageView imageView = new ImageView(texture);
            imageView.setFitWidth(BRIDGE_PREVIEW_WIDTH);
            imageView.setFitHeight(BRIDGE_PREVIEW_HEIGHT);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setRotate(90);

            StackPane preview = new StackPane(imageView);
            preview.setMinSize(BRIDGE_PREVIEW_WIDTH, BRIDGE_PREVIEW_HEIGHT);
            preview.setPrefSize(BRIDGE_PREVIEW_WIDTH, BRIDGE_PREVIEW_HEIGHT);
            preview.setMaxSize(BRIDGE_PREVIEW_WIDTH, BRIDGE_PREVIEW_HEIGHT);
            return preview;
        }

        Rectangle fallback = new Rectangle(BRIDGE_PREVIEW_WIDTH, BRIDGE_PREVIEW_HEIGHT);
        fallback.setFill(BridgeVisuals.colorFor(type));
        fallback.setStroke(Color.rgb(45, 45, 45));
        fallback.setArcWidth(4);
        fallback.setArcHeight(4);
        return fallback;
    }

    private void selectType(BridgeType type, HBox selectedRow) {
        selectedType = type;
        placeButton.setDisable(false);
        for (HBox row : rowNodes) {
            setRowSelectedStyle(row, row == selectedRow);
        }
    }

    private void setRowSelectedStyle(HBox row, boolean selected) {
        row.setStyle(selected
                ? "-fx-background-color: #E8F0FF; -fx-border-color: #4A90E2; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;"
                : "-fx-background-color: #F6F6F6; -fx-border-color: #C8C8C8; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");
    }

    private void placeSelectedBridge() {
        if (selectedType == null) {
            return;
        }
        if (onPlace != null) {
            onPlace.accept(selectedType);
        }
        close();
    }
}

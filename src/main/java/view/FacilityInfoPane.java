package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.Facility;
import model.Factory;
import model.GoodsType;
import model.Mine;

import java.util.Locale;

public class FacilityInfoPane extends Stage {
    private static final String TITLE_TEXT_STYLE = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #5b3a17;";
    private static final String ROW_TEXT_STYLE = "-fx-font-size: 12px; -fx-text-fill: #5b3a17;";
    private static final String ROOT_STYLE =
            "-fx-background-color: #f0c43c;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 0;" +
            "-fx-border-radius: 0;";

    private final Label nameLabel = new Label("Facility");
    private final VBox detailsBox = new VBox(4);

    public FacilityInfoPane() {
        setTitle("Facility Info");

        nameLabel.setStyle(TITLE_TEXT_STYLE);

        detailsBox.setAlignment(Pos.TOP_LEFT);
        detailsBox.setPadding(new Insets(4, 0, 0, 0));

        VBox root = new VBox(6, nameLabel, detailsBox);
        root.setPadding(new Insets(12));
        root.setStyle(ROOT_STYLE);

        Scene scene = new Scene(root, 320, 150);
        setScene(scene);
    }

    public void showForFacility(Facility facility, Window ownerWindow) {
        if (facility == null) {
            return;
        }
        if (getOwner() == null && ownerWindow != null) {
            initOwner(ownerWindow);
        }

        nameLabel.setText(buildFacilityName(facility));
        if (facility instanceof Factory) {
            int shortage = Math.max(0, facility.getProductionRate() - facility.getInputStock());
            detailsBox.getChildren().setAll(
                    row("Input Type", formatGoodsType(facility.getInputType())),
                    row("Output Type", formatGoodsType(facility.getOutputType())),
                    row("Shortage (Demand)", shortage + " " + formatGoodsType(facility.getInputType()))
            );
        } else {
            detailsBox.getChildren().setAll(
                    row("Input Type", formatGoodsType(facility.getInputType())),
                    row("Output Type", formatGoodsType(facility.getOutputType()))
            );
        }

        if (!isShowing()) {
            show();
        }
        toFront();
    }

    private Label row(String key, String value) {
        Label label = new Label(key + ": " + value);
        label.setStyle(ROW_TEXT_STYLE);
        return label;
    }

    private String buildFacilityName(Facility facility) {
        String outputName = formatGoodsType(facility.getOutputType());
        if (facility instanceof Mine) {
            return outputName + " Mine";
        }
        if (facility instanceof Factory) {
            return outputName + " Factory";
        }
        return outputName + " Facility";
    }

    private String formatGoodsType(GoodsType type) {
        if (type == null) {
            return "None";
        }
        String lower = type.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}

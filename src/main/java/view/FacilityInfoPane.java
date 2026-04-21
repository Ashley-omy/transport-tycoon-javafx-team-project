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
    private final Label nameLabel = new Label("Facility");
    private final VBox detailsBox = new VBox(4);

    public FacilityInfoPane() {
        setTitle("Facility Info");

        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        detailsBox.setAlignment(Pos.TOP_LEFT);
        detailsBox.setPadding(new Insets(4, 0, 0, 0));

        VBox root = new VBox(6, nameLabel, detailsBox);
        root.setPadding(new Insets(12));

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
        label.setStyle("-fx-font-size: 12px;");
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

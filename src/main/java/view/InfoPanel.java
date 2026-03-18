package view;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class InfoPanel extends VBox {

    private Label info = new Label("No selection");

    public InfoPanel() {
        getChildren().add(info);
    }

    public void update(String text) {
        info.setText(text);
    }
}

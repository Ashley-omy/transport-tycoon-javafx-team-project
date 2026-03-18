package view;

import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class BuildPanel extends VBox {

    public BuildPanel(UIState ui) {

        setSpacing(10);

        Button road = new Button("Build Road");
        Button stop = new Button("Build Stop");
        Button garage = new Button("Build Garage");
        Button route = new Button("Routes");

        road.setOnAction(e -> ui.setBuildMode(BuildMode.ROAD));
        stop.setOnAction(e -> ui.setBuildMode(BuildMode.STOP));
        garage.setOnAction(e -> ui.setBuildMode(BuildMode.GARAGE));

        getChildren().addAll(road, stop, garage, route);
    }
}
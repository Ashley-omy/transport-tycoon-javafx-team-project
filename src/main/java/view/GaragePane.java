package view;

import controller.ActionResult;
import controller.FleetController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.Bus;
import model.Company;
import model.Garage;
import model.Truck;
import model.Vehicle;
import model.VehicleState;

import java.util.function.Consumer;

public class GaragePane extends Stage {
    private final Company company;
    private final FleetController fleetController;
    private final Consumer<ActionResult> actionReporter;

    private final Label titleLabel = new Label("Garage");
    private final TilePane vehicleTiles = new TilePane();
    private final Label detailsLabel = new Label("Select a vehicle to see details.");
    private final Button buyButton = new Button("Buy");
    private final Button sellButton = new Button("Sell");
    private final Button resumeButton = new Button("Resume");

    private Garage currentGarage;
    private Vehicle selectedVehicle;

    public GaragePane(Company company, FleetController fleetController, Consumer<ActionResult> actionReporter) {
        this.company = company;
        this.fleetController = fleetController;
        this.actionReporter = actionReporter;

        setTitle("Garage");

        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        vehicleTiles.setPadding(new Insets(8));
        vehicleTiles.setHgap(10);
        vehicleTiles.setVgap(10);
        vehicleTiles.setPrefColumns(4);
        vehicleTiles.setTileAlignment(Pos.TOP_LEFT);

        ScrollPane listScroll = new ScrollPane(vehicleTiles);
        listScroll.setFitToWidth(true);
        listScroll.setPrefViewportHeight(260);

        detailsLabel.setWrapText(true);
        detailsLabel.setMinHeight(72);
        detailsLabel.setStyle("-fx-font-size: 12px;");

        buyButton.setDisable(true);
        sellButton.setDisable(true);
        resumeButton.setDisable(true);
        buyButton.setOnAction(e -> handleBuy());
        sellButton.setOnAction(e -> handleSell());
        resumeButton.setOnAction(e -> handleResume());

        HBox actions = new HBox(8, buyButton, sellButton, resumeButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox bottom = new VBox(10, detailsLabel, actions);
        bottom.setPadding(new Insets(10, 0, 0, 0));

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setTop(titleLabel);
        root.setCenter(listScroll);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 620, 430);
        setScene(scene);
    }

    public void showForGarage(Garage garage, Window ownerWindow) {
        if (garage == null) {
            return;
        }
        if (getOwner() == null && ownerWindow != null) {
            initOwner(ownerWindow);
        }

        currentGarage = garage;
        selectedVehicle = null;
        titleLabel.setText("Garage " + garage.getId());
        refreshVehicleTiles();
        updateSelectionUI();

        if (!isShowing()) {
            show();
        }
        toFront();
    }

    private void refreshVehicleTiles() {
        vehicleTiles.getChildren().clear();
        if (currentGarage == null) {
            return;
        }

        if (selectedVehicle != null && !currentGarage.getVehicles().contains(selectedVehicle)) {
            selectedVehicle = null;
        }

        if (currentGarage.getVehicles().isEmpty()) {
            Label empty = new Label("No vehicles in this garage.");
            empty.setStyle("-fx-text-fill: #666666;");
            vehicleTiles.getChildren().add(empty);
            return;
        }

        for (Vehicle vehicle : currentGarage.getVehicles()) {
            vehicleTiles.getChildren().add(createVehicleCard(vehicle));
        }
    }

    private VBox createVehicleCard(Vehicle vehicle) {
        boolean owned = isOwned(vehicle);
        boolean selected = isSelected(vehicle);

        Label ownedLabel = new Label(owned ? "OWNED" : "");
        ownedLabel.setTextFill(owned ? Color.FORESTGREEN : Color.TRANSPARENT);
        ownedLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Rectangle vehicleBody = new Rectangle(88, 42);
        vehicleBody.setArcWidth(8);
        vehicleBody.setArcHeight(8);
        vehicleBody.setFill(colorFor(vehicle));
        vehicleBody.setStroke(Color.BLACK);

        Label typeLabel = new Label(typeOf(vehicle));
        typeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Label idLabel = new Label(vehicle.getId().toString());
        idLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #555555;");

        VBox card = new VBox(6, ownedLabel, vehicleBody, typeLabel, idLabel);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(8));
        card.setPrefWidth(130);
        card.setStyle(selected
                ? "-fx-background-color: #EAF3FF; -fx-border-color: #4A90E2; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;"
                : "-fx-background-color: #F7F7F7; -fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

        card.setOnMouseClicked(e -> {
            selectedVehicle = vehicle;
            refreshVehicleTiles();
            updateSelectionUI();
        });

        return card;
    }

    private void updateSelectionUI() {
        if (selectedVehicle == null) {
            detailsLabel.setText("Select a vehicle to see details.");
            buyButton.setDisable(true);
            sellButton.setDisable(true);
            resumeButton.setDisable(true);
            return;
        }

        boolean owned = isOwned(selectedVehicle);
        buyButton.setDisable(owned);
        sellButton.setDisable(!owned);
        resumeButton.setDisable(!canResume(selectedVehicle, owned));
        detailsLabel.setText(buildDetails(selectedVehicle, owned));
    }

    private void handleBuy() {
        if (currentGarage == null || selectedVehicle == null) {
            return;
        }

        ActionResult result = fleetController.purchaseVehicleInGarage(currentGarage, selectedVehicle);
        reportAction(result);
        refreshVehicleTiles();
        updateSelectionUI();
    }

    private void handleSell() {
        if (selectedVehicle == null) {
            return;
        }

        ActionResult result = fleetController.sellVehicle(selectedVehicle.getId().toString());
        reportAction(result);
        refreshVehicleTiles();
        updateSelectionUI();
    }

    private void handleResume() {
        if (selectedVehicle == null) {
            return;
        }

        ActionResult result = fleetController.resumeVehicle(selectedVehicle.getId().toString());
        reportAction(result);
        refreshVehicleTiles();
        updateSelectionUI();
    }

    private void reportAction(ActionResult result) {
        if (actionReporter != null && result != null) {
            actionReporter.accept(result);
        }
    }

    private boolean isOwned(Vehicle vehicle) {
        return company.getFleet().contains(vehicle);
    }

    private boolean isSelected(Vehicle vehicle) {
        return selectedVehicle != null && selectedVehicle.getId().equals(vehicle.getId());
    }

    private String buildDetails(Vehicle vehicle, boolean owned) {
        return "Type: " + typeOf(vehicle)
                + "   Status: " + (owned ? "Owned" : "On sale")
                + "\nState: " + vehicle.getState()
                + "   Route: " + (vehicle.hasRoute() ? "Assigned" : "None")
                + "\nSpeed: " + vehicle.getSpeed() + " tiles/s"
                + "   Capacity: " + vehicle.getCapacityUnits()
                + "\nPrice: " + vehicle.getPurchaseCost()
                + "   Maintenance: " + vehicle.getMaintenanceCost();
    }

    private boolean canResume(Vehicle vehicle, boolean owned) {
        return owned
                && vehicle.hasRoute()
                && vehicle.getState() == VehicleState.IDLE;
    }

    private String typeOf(Vehicle vehicle) {
        if (vehicle instanceof Bus) {
            return "Bus";
        }
        if (vehicle instanceof Truck) {
            return "Truck";
        }
        return vehicle.getClass().getSimpleName();
    }

    private Color colorFor(Vehicle vehicle) {
        if (vehicle instanceof Bus) {
            return Color.DODGERBLUE;
        }
        if (vehicle instanceof Truck) {
            return Color.DARKORANGE;
        }
        return Color.DARKSLATEGRAY;
    }
}

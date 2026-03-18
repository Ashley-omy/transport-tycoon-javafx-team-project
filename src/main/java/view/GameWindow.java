/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */

import controller.*;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.*;

import javafx.scene.layout.*;
import javafx.geometry.Insets;

public class GameWindow extends BorderPane {

    private HUDView hud;
    private MapView mapView;
    private MinimapView minimap;
    private BuildPanel buildPanel;
    private InfoPanel infoPanel;
    private UIState ui;

    public GameWindow() {
        this.ui = new UIState();

        this.hud = new HUDView();
        this.mapView = new MapView();
        this.minimap = new MinimapView();
        this.buildPanel = new BuildPanel(ui);
        this.infoPanel = new InfoPanel();

        setTop(hud);

        setLeft(buildPanel);

        setCenter(mapView);

        // mini map
        VBox rightPanel = new VBox(10, minimap, infoPanel);
        rightPanel.setPadding(new Insets(10));
        setRight(rightPanel);
    }

    public void render(model.Game game, controller.SelectionController sel) {
        mapView.render(game.getWorld().getMap());
        minimap.render(game.getWorld().getMap());
        hud.render(game.getCompany(), game.getTick());
        //ui.syncFromSelection(sel);
    }

    public MapView getMapView() { return mapView; }
    public UIState getUIState() { return ui; }
}
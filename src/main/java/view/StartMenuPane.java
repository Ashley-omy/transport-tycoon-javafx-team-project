package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StartMenuPane extends StackPane {
    private static final String LOGO_PATH = "/assets/logo.png";
    private static final double MENU_LOGO_WIDTH = 360.0;
    private static final String ROOT_STYLE =
            "-fx-background-color: #fff7bf;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #ccb86a;" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;";
    private static final String TITLE_STYLE = "-fx-text-fill: #2e2a1a;";
    private static final String BODY_STYLE = "-fx-text-fill: #2e2a1a; -fx-font-size: 16px;";
    private static final double BUTTON_WIDTH = 260.0;
    private static final double BUTTON_HEIGHT = 34.0;
    private static final double CONTENT_WIDTH = 520.0;
    private static final double PANE_WIDTH = 620.0;
    private static final double PANE_HEIGHT = 420.0;

    private final Runnable onStartNewGameRequested;
    private final Runnable onResumeRequested;
    private final boolean hasSavedGame;
    private final VBox menuView;
    private final VBox howToPlayView;

    public StartMenuPane(boolean hasSavedGame, Runnable onStartNewGameRequested, Runnable onResumeRequested) {
        this.onStartNewGameRequested = onStartNewGameRequested == null ? () -> { } : onStartNewGameRequested;
        this.onResumeRequested = onResumeRequested == null ? () -> { } : onResumeRequested;
        this.hasSavedGame = hasSavedGame;
        this.menuView = createMenuView();
        this.howToPlayView = createHowToPlayView();

        setStyle(ROOT_STYLE);
        setPadding(new Insets(12));
        setAlignment(Pos.CENTER);
        setMaxWidth(PANE_WIDTH);
        setPrefWidth(PANE_WIDTH);
        setMinWidth(PANE_WIDTH);
        setMaxHeight(PANE_HEIGHT);
        setPrefHeight(PANE_HEIGHT);
        setMinHeight(PANE_HEIGHT);
        getChildren().setAll(menuView);
    }

    private VBox createMenuView() {
        ImageView logoView = createLogoView(MENU_LOGO_WIDTH);
        Label title = new Label("Transport Tycoon");
        title.setFont(Font.font("Segoe Print", FontWeight.BOLD, 15));
        title.setStyle(TITLE_STYLE);

        Button startNewGameButton = createMenuButton("Start New Game");
        startNewGameButton.setOnAction(event -> onStartNewGameRequested.run());

        Button howToPlayButton = createMenuButton("How to Play");
        howToPlayButton.setOnAction(event -> showHowToPlay());

        VBox buttons = new VBox(8);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().add(startNewGameButton);
        if (hasSavedGame) {
            Button resumeButton = createMenuButton("Resume Game");
            resumeButton.setOnAction(event -> onResumeRequested.run());
            buttons.getChildren().add(resumeButton);
        }
        buttons.getChildren().add(howToPlayButton);

        VBox menu = new VBox(14, logoView, title, buttons);
        menu.setAlignment(Pos.CENTER);
        return menu;
    }

    private VBox createHowToPlayView() {
        Label goalTitle = createSectionTitle("Goal");
        Label goalBody = createBodyLabel(
                "Build a successful transport company by transporting passengers and goods between cities and facilities.\n" +
                "Earn money by creating efficient routes, managing vehicles, and expanding your road network."
        );

        Label basicRulesTitle = createSectionTitle("Basic Rules");
        Label rulesBody = createBodyLabel(
                "Build roads to connect cities, stops, garages, and facilities.\n" +
                "Place garages along roads to purchase and manage vehicles.\n" +
                "Build stops near cities or facilities to collect passengers and cargo.\n" +
                "Purchase vehicles from garages and select stops to create transport routes.\n" +
                "Vehicles automatically transport passengers and goods along their assigned routes.\n" +
                "Deliveries generate income based on cargo value.\n" +
                "Vehicles require maintenance over time. Old vehicles become less efficient and can be sold.\n" +
                "Running out of money results in bankruptcy and game over."
        );

        Button backButton = createMenuButton("Back");
        backButton.setFont(Font.font("Segoe Print", 16));
        backButton.setOnAction(event -> showMenu());
        StackPane backContainer = new StackPane(backButton);
        backContainer.setAlignment(Pos.CENTER);
        backContainer.setMaxWidth(Double.MAX_VALUE);

        VBox howTo = new VBox(10, goalTitle, goalBody, basicRulesTitle, rulesBody, backContainer);
        howTo.setAlignment(Pos.CENTER_LEFT);
        howTo.setFillWidth(true);
        howTo.setMaxWidth(CONTENT_WIDTH);

        ScrollPane scrollPane = new ScrollPane(howTo);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefViewportHeight(PANE_HEIGHT - 40);

        VBox container = new VBox(scrollPane);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(CONTENT_WIDTH + 20);
        return container;
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setMinSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setMaxSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setFocusTraversable(false);
        return button;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle(TITLE_STYLE);
        label.setFont(Font.font("Segoe Print", FontWeight.BOLD, 28));
        return label;
    }

    private Label createBodyLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle(BODY_STYLE);
        label.setFont(Font.font("Segoe Print", 16));
        label.setMaxWidth(CONTENT_WIDTH);
        return label;
    }

    private ImageView createLogoView(double fitWidth) {
        var resource = StartMenuPane.class.getResource(LOGO_PATH);
        ImageView logoView = new ImageView();
        if (resource == null) {
            return logoView;
        }
        Image image = new Image(resource.toExternalForm());
        logoView.setImage(image);
        logoView.setFitWidth(fitWidth);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        return logoView;
    }

    private void showHowToPlay() {
        setAlignment(Pos.CENTER);
        getChildren().setAll(howToPlayView);
    }

    private void showMenu() {
        setAlignment(Pos.CENTER);
        getChildren().setAll(menuView);
    }
}

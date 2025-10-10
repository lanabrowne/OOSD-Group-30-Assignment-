package org.oosd.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import org.oosd.Main;
import org.oosd.controller.TwoPlayerAIController;
import org.oosd.model.Board;
import org.oosd.model.GameBoardAdapter;

public class TwoPlayerAIScreen implements ScreenWithGame {

    private final HBox root;
    private final TwoPlayerAIController ai1;
    private final TwoPlayerAIController ai2;

    private Screen parentScreen;
    private Main mainApp;

    public TwoPlayerAIScreen(Main mainApp, Screen parentScreen) {
        this.mainApp = mainApp;
        this.parentScreen = parentScreen;

        Board board1 = new Board(10, 20);
        Board board2 = new Board(10, 20);

        javafx.scene.canvas.Canvas c1 = new javafx.scene.canvas.Canvas(10 * 30, 20 * 30);
        javafx.scene.canvas.Canvas c2 = new javafx.scene.canvas.Canvas(10 * 30, 20 * 30);

        GameBoardAdapter adapter1 = new GameBoardAdapter(board1);
        GameBoardAdapter adapter2 = new GameBoardAdapter(board2);

        ai1 = new TwoPlayerAIController(adapter1, c1);
        ai2 = new TwoPlayerAIController(adapter2, c2);

        Line separator = new Line(0, 0, 0, 600);
        separator.setStroke(Color.WHITE);
        separator.setStrokeWidth(2);

        root = new HBox(20, c1, separator, c2);

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            private static final long INTERVAL = 300_000_000;

            @Override
            public void handle(long now) {
                if (now - lastUpdate > INTERVAL) {
                    ai1.step();
                    ai2.step();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
    }

    @Override
    public void setRoute(String route, Screen parent) {
        this.parentScreen = parent;
    }

@Override
public Parent getScreen() {
    javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(10); // spacing 10px
    container.setStyle("-fx-background-color: white;"); // optional background

    container.getChildren().add(root);

    Button backButton = new Button("← Back");
    backButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

    backButton.setOnAction(e -> {
        if (mainApp != null && parentScreen != null) {
            mainApp.showScreen(parentScreen);
        }
    });

    container.getChildren().add(backButton);
    container.setAlignment(javafx.geometry.Pos.CENTER);

    return container;
}

    @Override
    public void initializeGameController(org.oosd.controller.GameController controller) {
    }
}

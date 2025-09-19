package org.oosd.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import org.oosd.Main;

public class TwoPlayerScreen implements Screen {

    private VBox root;
    private Screen backRoute;
    private Main parent;

    public TwoPlayerScreen(Main main) {
        this.parent = main;
        buildScreen();
    }

    private void buildScreen() {
        root = new VBox(10);
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Two Player Tetris");
        title.getStyleClass().add("title");

        HBox boards = new HBox(20);
        boards.setAlignment(Pos.CENTER);

        VBox player1Board = new VBox();
        player1Board.setStyle("-fx-border-color: black; -fx-background-color: lightblue; -fx-min-width: 250; -fx-min-height: 500;");
        player1Board.getChildren().add(new Label("Player 1 Board"));

        VBox player2Board = new VBox();
        player2Board.setStyle("-fx-border-color: black; -fx-background-color: lightgreen; -fx-min-width: 250; -fx-min-height: 500;");
        player2Board.getChildren().add(new Label("Player 2 Board"));

        boards.getChildren().addAll(player1Board, player2Board);

        root.getChildren().addAll(title, boards);
    }

    @Override
    public Parent getScreen() {
        return root;
    }

    @Override
    public void setRoute(String path, Screen screen) {
        if ("back".equals(path)) backRoute = screen;
    }
}

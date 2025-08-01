package org.oosd;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("");
        //TextField input = new TextField();
        Button btn = new Button("Click Me");
        Button game = new Button("Play Game");
        Button conf = new Button("Configurations");
        Button exit = new Button("Exit");
        Button random = new Button("random");


        StackPane root = new StackPane();
        root.getChildren().addAll(label, btn, game, conf, exit, random);
        btn.setOnAction(e -> {
            int x = 10;
            int y = x + 5;
            System.out.println(x + y);
            label.setText("Welcome to Tetris");
//            label.setText("Hello, " + input.getText() + "!");
        });
        StackPane.setAlignment(conf, Pos.CENTER_RIGHT);
        StackPane.setAlignment(game, Pos.CENTER);
        StackPane.setAlignment(exit, Pos.CENTER_LEFT);
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tetris");
        primaryStage.show();
    }
}
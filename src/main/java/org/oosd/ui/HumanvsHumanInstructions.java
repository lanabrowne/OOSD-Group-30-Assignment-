package org.oosd.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import org.oosd.controller.TwoPlayerController;
import javafx.scene.Node;

import java.io.IOException;

public class HumanvsHumanInstructions implements Screen {

    private StackPane root;       // StackPane holds both overlay and game!
    private TwoPlayerController game; // controller from FXML

    public HumanvsHumanInstructions(Frame frame) {
       try {
           //"/org.oosd/fxml/HvHGameScreen.fxml"
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.oosd/fxml/HvHGameScreen.fxml"));

           if(loader == null){
               System.out.println("File could not be found");
           }else
           {
               System.out.println("File found");

           }
           Parent gameRoot = loader.load();


           game = loader.getController();
           game.setParent(frame);
           // Pause the game until overlay closes/is dismissed
           game.pauseGame();

           // purely Overlay code
           VBox overlay = new VBox(15);
           overlay.setAlignment(Pos.CENTER);
           overlay.setPadding(new Insets(40));
           overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

           Label bigTitle = new Label("TETRIS");
           bigTitle.setTextFill(Color.WHITE);
           bigTitle.setFont(Font.font(40));

           Label player1Keys = new Label("Player 1: A/D move   W rotate   S soft drop");
           Label player2Keys = new Label("Player 2: ← → move   ↑ rotate   ↓ soft drop");
           Label startHint = new Label("Press any key to start • P to pause");

           for (Label l : new Label[]{player1Keys, player2Keys, startHint}) {
               l.setTextFill(Color.LIGHTGRAY);
               l.setFont(Font.font(18));
           }

            // Pause overlay
            VBox pauseOverlay = new VBox(15);
            pauseOverlay.setAlignment(Pos.CENTER);
            pauseOverlay.setPadding(new Insets(40));
            pauseOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

            Label pauseLabel = new Label("PAUSED");
            pauseLabel.setTextFill(Color.WHITE);
            pauseLabel.setFont(Font.font(40));

            Label pauseHint = new Label("Press P to resume");
            pauseHint.setTextFill(Color.LIGHTGRAY);
            pauseHint.setFont(Font.font(18));

            pauseOverlay.getChildren().addAll(pauseLabel, pauseHint);

           overlay.getChildren().addAll(bigTitle, player1Keys, player2Keys, startHint);

           // StackPane is used for adding overlay and back button
           this.root = new StackPane(gameRoot, overlay);

           // Back button
           Button backButton = new Button("Back");
           backButton.setStyle("-fx-font-size: 16px; -fx-background-color: black;");
           backButton.setOnAction(e ->
                   // Use Frame interface method to navigate back to main screen
                   frame.showScreen(frame.getMainScreen()));
           StackPane.setAlignment(backButton, Pos.BOTTOM_CENTER);
           StackPane.setMargin(backButton, new Insets(0, 0, 20, 0));
           this.root.getChildren().add(backButton);

           this.root.setFocusTraversable(true);

           this.root.sceneProperty().addListener((obs, oldScene, scene) -> {
    if (scene != null) {
        this.root.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case P -> {
                    if (root.getChildren().contains(pauseOverlay)) {
                        // Resume game
                        root.getChildren().remove(pauseOverlay);
                        game.resumeGame();
                    } else {
                        // Pause game
                        root.getChildren().add(pauseOverlay);
                        game.pauseGame();
                    }
                    e.consume();
                }
                default -> {
                    if (root.getChildren().contains(overlay)) {
                        root.getChildren().remove(overlay);
                        game.resumeGame();
                        Object n = loader.getNamespace().get("leftColumn");
                        if (n instanceof Node node) {
                            Platform.runLater(node::requestFocus);
                        }
                        e.consume();
                    }
                }
            }
        });
        Platform.runLater(this.root::requestFocus);
    }
});

       } catch (IOException e){
           System.out.println("File Load Error" + e.getMessage());
            e.printStackTrace();
       }
    }

    @Override
    public Parent getScreen() {
        return root;
    }

    @Override
    public void setRoute(String path, Screen screen) {
        game.setRoute(path, screen);
    }
}
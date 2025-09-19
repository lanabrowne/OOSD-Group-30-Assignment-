package org.oosd.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import org.oosd.Main;
import org.oosd.ui.Screen;

public class TwoPlayerInstructions implements Screen {

    private StackPane root;       // StackPane holds both overlay and game!
    private TwoPlayerTetris game; 

    public TwoPlayerInstructions(Frame frame) {
        game = new TwoPlayerTetris(frame);

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
        Label startHint   = new Label("Press any key to start • P to pause");

        for (Label l : new Label[]{player1Keys, player2Keys, startHint}) {
            l.setTextFill(Color.LIGHTGRAY);
            l.setFont(Font.font(18));
        }

        overlay.getChildren().addAll(bigTitle, player1Keys, player2Keys, startHint);

        // StackPane is used for adding overlay and back button
        root = new StackPane(game, overlay);

        // Back button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 16px; -fx-background-color: black;");
        backButton.setOnAction(e -> {
            // Use Frame interface method to navigate back to main screen
            frame.showScreen(frame.getMainScreen());
        });
        StackPane.setAlignment(backButton, Pos.BOTTOM_CENTER);
        StackPane.setMargin(backButton, new Insets(0, 0, 20, 0)); 
        root.getChildren().add(backButton);

        // Removes overlay on any key press and start the game
        root.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (root.getChildren().contains(overlay)) {
                root.getChildren().remove(overlay);
                game.resumeGame();  
                game.requestFocus();
            }
        });

        Platform.runLater(() -> root.requestFocus());
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

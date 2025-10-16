package org.oosd.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import org.oosd.Main;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfig;

public class MainScreen implements Screen {


    private VBox mainScreen;
    private Screen configScreen, highScoreScreen;
    private Frame parent;

    @Override
    public Parent getScreen() {return mainScreen;}

    @Override
    public void setRoute(String path, Screen screen){
        switch (path) {
            case "config" -> configScreen = screen;
            case "highscores" -> highScoreScreen = screen;
            default -> {
            }
        }
    }

    public MainScreen(Frame frame){
        parent = frame;
        buildScreen();
    }

    private void buildScreen() {
        mainScreen = new VBox(10);
        // add background images css
        mainScreen.getStyleClass().add("wrapper");
        mainScreen.setAlignment(Pos.CENTER);
        // Title
        Label title = new Label("Tetris");
        title.getStyleClass().add("title");
        //title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        // Buttons
        Button btn = new Button("Click Me");
        Button gameButton = new Button("Play Game");
                Button confButton = new Button("Configurations");
        Button highScoresButton = new Button("High Scores");
        Button exitButton = new Button("Exit");

        // button functionality
        confButton.setOnAction(e -> parent.showScreen(configScreen));
        gameButton.setOnAction(e -> {
            if (parent instanceof Main m) {
                /**
                 * NEW
                 * to show different game screen depends on user choice,
                 * create if statement
                 */
                //Collect user choice from Jason file
                TetrisConfig config = ConfigService.get();
                //If user did not extended mode --> solo play
                if(!config.extendMode())
                {
                    m.showNewGame(); // calls the helper in Main that builds a new GameScreen
                }else{
                    //Else --> Two Player mode
                    m.showTwoPlayerGame();
                }

            }
        });
        highScoresButton.setOnAction(e -> parent.showScreen(highScoreScreen));
        exitButton.setOnAction(e-> parent.showExitConfirmation());
        /*
        when back is pressed, if it is pressed on the menu screen-
        a confirmation screen will appear yes -> leaves game no -> nothing happens
        if the back button is pressed on any other screen,
        the player will return to the main screen
         */
//        exitButton.setOnAction(e -> {
//            if (confirmExit()){
//                Platform.exit();
//            } else {
//                showMainScreen();
//            }
//        });

        // sets the button sizes
        gameButton.setPrefWidth(200);
        confButton.setPrefWidth(200);
        exitButton.setPrefWidth(200);
        highScoresButton.setPrefWidth(200);


        // links buttons to screen
        mainScreen.getChildren().addAll(title, gameButton, confButton, highScoresButton, exitButton);
    }
}

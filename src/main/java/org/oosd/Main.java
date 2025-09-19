package org.oosd;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfigView;
import org.oosd.ui.*;

import java.util.Optional;

public class Main extends Application implements Frame {

    private StackPane root;
    private MainScreen mainScreen;
    private static final double fieldWidth = 500;
    private static final double fieldHeight = 750;
    private AnimationTimer timer;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Build all top-level screens and wire up navigation.
     */
 private void buildScreens() {
    mainScreen = new MainScreen(this);

    ConfigScreen configScreen = new ConfigScreen(
        () -> showNewGame(),        // single-player callback
        () -> showTwoPlayerGame(),  // two-player callback
        () -> showScreen(mainScreen) // back callback
    );

    HighScoreScreen highScoreScreen = new HighScoreScreen(this);

    mainScreen.setRoute("config", configScreen);
    mainScreen.setRoute("highscores", highScoreScreen);
    configScreen.setRoute("back", mainScreen);
    highScoreScreen.setRoute("back", mainScreen);

    showScreen(mainScreen);
}

    /* Start a fresh single-player game. */
    public void showNewGame() {
        GameScreen newGame = new GameScreen(this);
        newGame.setRoute("back", mainScreen);
        showScreen(newGame);
    }

    /* Start a fresh two-player game after overlay is dismissed. */
    public void showTwoPlayerGame() {
    System.out.println("DEBUG: entering showTwoPlayerGame()");
    // Show overlay first, then the game starts after key press
    TwoPlayerInstructions overlayScreen = new TwoPlayerInstructions(this);
    showScreen(overlayScreen);
}



    @Override
    public void start(Stage primaryStage) throws Exception {
        ConfigService.load();
        Font.loadFont(Main.class.getResource("/fonts/Montserrat-Black.ttf").toExternalForm(), 120);

        root = new StackPane();
        Scene scene = new Scene(root, fieldWidth, fieldHeight);
        scene.getStylesheets().add(Main.class.getResource("/org.oosd/css/styles.css").toExternalForm());

        primaryStage.setTitle("Tetris");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Splash first, then menu/config
        SplashScreen.show(primaryStage, this::buildScreens, fieldWidth, fieldHeight);

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            showExitConfirmation();
        });
    }

    public void showScreen(Screen scr) {
        root.getChildren().setAll(scr.getScreen());
    }

    @Override
    public void showExitConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Confirm Exit");
        alert.setContentText("Are you sure you want to exit?");

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            System.exit(0);
        }
    }
@Override
public MainScreen getMainScreen() {
    return mainScreen;
}

}

package org.oosd;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.oosd.audio.audioManager;
import org.oosd.config.ConfigService;
import org.oosd.ui.*;

import java.util.Objects;
import java.util.Optional;

public class Main extends Application implements Frame {

    private StackPane root;
    private MainScreen mainScreen;
    private static final double fieldWidth = 500;
    private static final double fieldHeight = 750;

    private Text musicIndicator;
    private Text sfxIndicator;

    public static void main(String[] args) {
        launch(args);
    }

    public void showTwoPlayerAI() {
        TwoPlayerAIScreen aiScreen = new TwoPlayerAIScreen(this, mainScreen);
        showScreen(aiScreen);
    }

    private void buildScreens() {
        mainScreen = new MainScreen(this);

        ConfigScreen configScreen = new ConfigScreen(
                () -> showNewGame(),
                () -> showTwoPlayerGame(),
                () -> showTwoPlayerAI(),
                () -> showScreen(mainScreen)
        );

        HighScoreScreen highScoreScreen = new HighScoreScreen(this);

        mainScreen.setRoute("config", configScreen);
        mainScreen.setRoute("highscores", highScoreScreen);
        configScreen.setRoute("back", mainScreen);
        highScoreScreen.setRoute("back", mainScreen);

        showScreen(mainScreen);

        audioManager.getInstance().playMusic();
    }

    public void showNewGame() {
        GameScreen newGame = new GameScreen(this);
        newGame.setRoute("back", mainScreen);
        showScreen(newGame);
    }

    public void showTwoPlayerGame() {
        HumanvsHumanInstructions overlayScreen = new HumanvsHumanInstructions(this);
        showScreen(overlayScreen);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ConfigService.load();
        Font.loadFont(Main.class.getResource("/fonts/Montserrat-Black.ttf").toExternalForm(), 120);

        root = new StackPane();
        Scene scene = new Scene(root, fieldWidth, fieldHeight);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        Main.class.getResource("/org/oosd/css/styles.css")
                ).toExternalForm()
        );

        // Music and SFX indicators
        musicIndicator = new Text();
        musicIndicator.setFill(Color.WHITE);
        musicIndicator.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        sfxIndicator = new Text();
        sfxIndicator.setFill(Color.WHITE);
        sfxIndicator.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Position them
        musicIndicator.setTranslateX(10);
        musicIndicator.setTranslateY(10);

        sfxIndicator.setTranslateX(10);
        sfxIndicator.setTranslateY(30);

        root.getChildren().addAll(musicIndicator, sfxIndicator);

        updateAudioIndicators();

        // Key listener for M/S toggles
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case M -> {
                    audioManager.getInstance().toggleMusic();
                    showToggleMessage("Music: " + (audioManager.getInstance().isMusicEnabled() ? "ON" : "OFF"));
                    updateAudioIndicators();
                }
                case S -> {
                    audioManager.getInstance().toggleSFX();
                    showToggleMessage("SFX: " + (audioManager.getInstance().isSfxEnabled() ? "ON" : "OFF"));
                    updateAudioIndicators();
                }
            }
        });

        primaryStage.setTitle("Tetris");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

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
            audioManager.getInstance().stopMusic();
            System.exit(0);
        }
    }

    @Override
    public MainScreen getMainScreen() {
        return mainScreen;
    }

    private void showToggleMessage(String message) {
        Text text = new Text(message);
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        root.getChildren().add(text);

        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> root.getChildren().remove(text));
        delay.play();
    }

 private void updateAudioIndicators() {
    // Update indicator text
    musicIndicator.setText("🎵 " + (audioManager.getInstance().isMusicEnabled() ? "ON" : "OFF"));
    sfxIndicator.setText("🔊 " + (audioManager.getInstance().isSfxEnabled() ? "ON" : "OFF"));

    // Position them above the game board
    musicIndicator.setTranslateX(10);   // margin from left
    musicIndicator.setTranslateY(-fieldHeight / 2 + 30); // top of game board

    sfxIndicator.setTranslateX(10);
    sfxIndicator.setTranslateY(-fieldHeight / 2 + 60); // just below music indicator
}


}

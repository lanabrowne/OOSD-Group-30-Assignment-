package org.oosd;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfigView;
import org.oosd.ui.*;

import java.io.IOException;
import java.util.Optional;

public class Main extends Application implements Frame {

    /**
     * menu screen and config screen were used
     * get children command to use and fxml file screen was used root command
     * to show. Using different command to show the screen was made error when
     * we wanted to back to menu screen from another page because:
     *
     * Menu screen (Children node)
     * ↓↓
     * ↓↓ when we back to menu screen from Game screen,
     * ↓↓ Root will return to null because Menu is not root.
     * GameScreen (Root)
     *
     * So that we need to use only one (root) to move to all screens.
     */

    // Global Variables
    private StackPane root;
    private MainScreen mainScreen;
    //private static Stage primaryStage;
    private static Scene scene;
    private static final double fieldWidth = 500;
    private static final double fieldHeight = 750;

    private AnimationTimer timer;

    /**
     * This is the main class to manage the execution and each screen.
     * By using root, navigating to all pages.
     * 
     * @param args
     */

    public static void main(String[] args) {
        launch(args);
    }

    private void buildScreens(){
        mainScreen = new MainScreen(this);
//        ScreenWithGame gameScreen = new GameScreen(this);
        ConfigScreen configScreen = new ConfigScreen(this);
        HighScoreScreen highScoreScreen = new HighScoreScreen(this);

        //main screen routes
        mainScreen.setRoute("game", null); // placeholder, handled dynamically
        mainScreen.setRoute("config", configScreen);
        mainScreen.setRoute("highscores", highScoreScreen);
        // Routes back to main
//        gameScreen.setRoute("back", mainScreen);
        configScreen.setRoute("back", mainScreen);
        highScoreScreen.setRoute("back", mainScreen);

        // Show main screen first
        showScreen(mainScreen);
    }

    public void showNewGame() {
        GameScreen newGame = new GameScreen(this);
        newGame.setRoute("back", mainScreen);
        showScreen(newGame);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//      game = new Game();
        ConfigService.load();
        Font.loadFont(Main.class.getResource("/fonts/Montserrat-Black.ttf").toExternalForm(), 120);

        // Creates root of application
        root = new StackPane();
        Scene scene = new Scene(root, fieldWidth, fieldHeight);
        scene.getStylesheets().add(Main.class.getResource("/org.oosd/css/styles.css").toExternalForm());
        primaryStage.setTitle("Tetris");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Show splash screen first, then build main menu
        SplashScreen.show(primaryStage, this::buildScreens, fieldWidth, fieldHeight);

        primaryStage.setOnCloseRequest(event ->{
            event.consume();
            showExitConfirmation();
        });



    }

    public void showScreen(Screen scr){
        root.getChildren().setAll(scr.getScreen());
    }

    public MainScreen getMainScreen(){
        return mainScreen;
    }





//    public static void initializeMenuScreen()
//    {
//
//
//        Parent menu = menuForm();
//        scene = new Scene(menu, fieldWidth, fieldHeight);
//        scene.getStylesheets().add(Main.class.getResource("/org.oosd/css/styles.css").toExternalForm());
//        // adds title
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Tetris");
//        // creates the overall box
//        primaryStage.show();
//    }

    /**
     * This method shows menu screen when application is executed.
     * by using static void, we can call this method from another class and
     * event to back to this screen from another screen.
     */
//    public static void showMainScreen() {
//        // Moved design code of menu screen to another method to set root f
//        // Use scene.setRoot command to switch the screen to menu screen.
//        scene.setRoot(menuForm());
//    }

    /**
     * This method is also showing config screen when user clicked
     * config button from menu UI. This method should be private not to be used
     * at another class.
     */
//    private static void showConfigScreen() {
//    TetrisConfigView view = new TetrisConfigView();
//    // using VBox to design of config screen
//        Parent root = view.buildConfigRoot(() -> main.showScreen(main.getMainScreen()));
//        // switch to config screen from menu screen using by root
//        scene.setRoot(root);
//    }

    /**
     * This method is open the tetris game screen read by fxml file
     */


    /**
     * This is the method that showing game score screen.
     * Had to make method public so that the end game button could directly link to this screen
     */
    public static void showHighScoreScreen() {
        // Just for the checking buttons action
        System.out.println("High score button clicked!");

        try {
            // This is also used fxml loader to load fxml file which is designed for game
            // score screen
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/org.oosd/fxml/GameScoreScreen.fxml"));
            // Load the file and convert to parent as well
            Parent highScoreRoot = loader.load();
            System.out.println("FXML loaded successfully!");
            scene.setRoot(highScoreRoot);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
        if(result.isPresent() && result.get() == yesButton){
            System.exit(0);
        }
        //return result.isPresent() && result.get() == yesButton;
    }
}

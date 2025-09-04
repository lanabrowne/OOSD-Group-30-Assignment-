package org.oosd;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.oosd.config.ConfigService;
import org.oosd.config.TetrisConfigView;
import org.oosd.controller.GameController;
import org.oosd.controller.GameScoreController;

import java.io.IOException;
import java.util.Optional;

public class Main extends Application {

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
    private static Stage primaryStage;
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

    @Override
    public void start(Stage stage) throws Exception {

        ConfigService.load();
        primaryStage = stage;
        // pass Stage object to splash screen class
        buildSplashScreen(stage);
        //Loads font for main screen
        Font.loadFont(Main.class.getResource("/fonts/Montserrat-Black.ttf").toExternalForm(), 120);

    }

    //Builds splash screen
    private static void buildSplashScreen(Stage stage){

        primaryStage = stage;
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setAlwaysOnTop(true);

        BorderPane splashScreen = new BorderPane();
        splashScreen.setStyle("-fx-background-color: white;");

        // load images
        Image tetrisTopImg = new Image(Main.class.getResourceAsStream("/Images/TetrisSplashScreenTop.jpg"));
        Image tetrisBottomImg = new Image(Main.class.getResourceAsStream("/Images/TetrisSplashScreenBottom.jpg"));

        // Top banner
        ImageView topImg = new ImageView(tetrisTopImg);
        topImg.setPreserveRatio(true);
        splashScreen.setTop(topImg);
        BorderPane.setAlignment(topImg, Pos.CENTER);

        // Bottom banner
        ImageView bottomImg = new ImageView(tetrisBottomImg);
        bottomImg.setPreserveRatio(true);
        splashScreen.setBottom(bottomImg);
        BorderPane.setAlignment(bottomImg, Pos.CENTER);

        // Group information text in centre of screen
        VBox textBox = new VBox(10);
        textBox.setAlignment(Pos.CENTER);
        Text groupInfo = new Text(
                "Group ID: PG 30\n" +
                        "Members:\n" +
                        "s5340293, Lana Browne, 2805ICT\n" +
                        "s5350825, Taylor Brown, 2006ICT\n" +
                        "s5404819, Ria Rajesh, 2006ICT\n" +
                        "s5339308, Ikkei Fukuta, 2006ICT\n" +
                        "s5373939, Kosuke Sato, 2006ICT"
        );
        groupInfo.setStyle("-fx-fill: black; -fx-font-size: 22px;");
        textBox.getChildren().add(groupInfo);
        splashScreen.setCenter(textBox);

        // Scene and show
        Scene splashScene = new Scene(splashScreen, fieldWidth, fieldHeight);
        topImg.fitWidthProperty().bind(splashScene.widthProperty());
        bottomImg.fitWidthProperty().bind(splashScene.widthProperty());

        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();
        splashStage.show();

        // Simulate loading task (keep as-is)
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(4000);
                return null;
            }
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    splashStage.close();
                    initializeMenuScreen();
                });
            }
        };
        new Thread(loadTask).start();

    }



    public static void initializeMenuScreen()
    {


        Parent menu = menuForm();
        scene = new Scene(menu, fieldWidth, fieldHeight);
        scene.getStylesheets().add(Main.class.getResource("/org.oosd/css/styles.css").toExternalForm());
        // adds title
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tetris");
        // creates the overall box
        primaryStage.show();
    }

    /**
     * This method shows menu screen when application is executed.
     * by using static void, we can call this method from another class and
     * event to back to this screen from another screen.
     */
    public static void showMainScreen() {
        // Moved design code of menu screen to another method to set root f
        // Use scene.setRoot command to switch the screen to menu screen.
        scene.setRoot(menuForm());
    }

    /**
     * This method is also showing config screen when user clicked
     * config button from menu UI. This method should be private not to be used
     * at another class.
     */
    private static void showConfigScreen() {
        TetrisConfigView view = new TetrisConfigView();
        // using VBox to design of config screen
        Parent root = view.buildConfigRoot(() -> showMainScreen());
        // switch to config screen from menu screen using by root
        scene.setRoot(root);
    }

    /**
     * This method is open the tetris game screen read by fxml file
     */
    private static void showGameScreen() {
        try {
            // read fxml file (design of game screen) used by FXML Loader command
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/org.oosd/fxml/GameScreen.fxml"));
            // Load the file and convert to parent node
            Parent game = loader.load();
            // and to valid functions, obtain the controller which is related to this fxml
            // file
            // (GameController) --> then we can control all operations.
            GameController gc = loader.getController();
            // show the read game screen into UI
            scene.setRoot(game);

        } catch (IOException ex) {
            // Just show the error log when reading file was failed.
            ex.printStackTrace();
        }
    }

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

    /**
     * This method is just for the menu form design that located at show menu
     * screen.
     * this needed to be replaced to another method to use root navigation
     * management.
     * 
     * @return
     */
    private static Parent menuForm() {
        VBox mainScreen = new VBox(10);
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
        confButton.setOnAction(e -> showConfigScreen());
        gameButton.setOnAction(e -> showGameScreen());
        highScoresButton.setOnAction(e -> showHighScoreScreen());
        /*
        when back is pressed, if it is pressed on the menu screen-
        a confirmation screen will appear yes -> leaves game no -> nothing happens
        if the back button is pressed on any other screen,
        the player will return to the main screen
         */
        exitButton.setOnAction(e -> {
            if (confirmExit()){
                Platform.exit();
            } else {
                showMainScreen();
            }
        });

        // sets the button sizes
        gameButton.setPrefWidth(200);
        confButton.setPrefWidth(200);
        exitButton.setPrefWidth(200);
        highScoresButton.setPrefWidth(200);

        // Load a background image??

        // links buttons to screen
        mainScreen.getChildren().addAll(title, gameButton, confButton, highScoresButton, exitButton);
        return mainScreen;
    }




    public static boolean confirmExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Confirm Exit");
        alert.setContentText("Are you sure you want to exit?");

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }
}

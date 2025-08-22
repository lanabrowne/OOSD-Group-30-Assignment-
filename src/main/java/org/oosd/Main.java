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
import javafx.stage.Stage;
import org.oosd.controller.GameController;
import org.oosd.controller.GameScoreController;

import java.io.IOException;
import java.net.URL;


public class Main extends Application {

    /**
     * menu screen and config screen were used
     * get children command to use and fxml file screen was used root command
     * to show. Using different command to show the screen was made error when
     * we wanted to back to menu screen from another page because:
     *
     * Menu screen (Children node)
     *  　　　　↓↓
     *  　　　　↓↓  when we back to menu screen from Game screen,
     * 　　　　 ↓↓  Root will return to null because Menu is not root.
     * GameScreen (Root)
     *
     * So that we need to use only one (root) to move to all screens.
     */

    //Global Variables
    private StackPane root;
    private static Stage primaryStage;
    private static Scene scene;
    private static final double fieldWidth = 800;
    private static final double fieldHeight = 600;
    private AnimationTimer timer;
    private static GameController gameController;


    /**
     * This is the main class to manage the execution and each screen.
     * By using root, navigating to all pages.
     * @param args
     */

    public static void main(String[] args) {
        launch(args);
    }

    public class SplashDemo extends Application {

        @Override
        public void start(Stage primaryStage) {

        }


    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Splash
        Stage splashStage = new Stage();
        URL splashUrl = getClass().getResource("/Images/TetrisSplashScreen.jpg");
        if (splashUrl == null) throw new IllegalStateException("Splash image not found!");

        ImageView splashImage = new ImageView(new Image(splashUrl.toExternalForm()));
        splashImage.setFitWidth(300);
        splashImage.setFitHeight(300);
        splashImage.setPreserveRatio(true);
        Label loadingLabel = new Label("Loading...");
        StackPane splashLayout = new StackPane(splashImage, loadingLabel);
        Scene splashScene = new Scene(splashLayout, 300, 300);
        splashStage.setScene(splashScene);
        splashStage.show();

        // Simulate loading
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(3000);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    splashStage.close();
                    // Initialize scene for the first time
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
        if (scene != null) {
            scene.setRoot(menuForm());
        }
    }


    /**
     * This method is also showing config screen when user clicked
     * config button from menu UI. This method should be private not to be used
     * at another class.
     */
    private static void showConfigScreen() {
        // using VBox to design of config screen
        VBox config = buildConfigRoot();
        //switch to config screen from menu screen using by root
        scene.setRoot(config);
    }

    /**
     * This method is open the tetris game screen read by fxml file
     */
    private static void showGameScreen() {
        try {
            //read fxml file (design of game screen) used by FXML Loader command
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/org.oosd/fxml/GameScreen.fxml"));
            // Load the file and convert to parent node
            Parent game = loader.load();
            //and to valid functions, obtain the controller which is related to this fxml file
            //(GameController) --> then we can control all operations.
            GameController gc = loader.getController();
            //show the read game screen into UI
            scene.setRoot(game);




        } catch (IOException ex) {
            //Just show the error log when reading file was failed.
            ex.printStackTrace();
        }
    }

    /**
     * This is the method that showing game score screen.
     */
    private static void showHighScoreScreen() {
        //Just for the checking buttons action
        System.out.println("High score button clicked!");

        try {
            //This is also used fxml loader to load fxml file which is designed for game score screen
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/org.oosd/fxml/GameScoreScreen.fxml"));
            //Load the file and convert to parent as well
            Parent highScoreRoot = loader.load();
            System.out.println("FXML loaded successfully!");
            scene.setRoot(highScoreRoot);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is just for the menu form design that located at show menu screen.
     * this needed to be replaced to another method to use root navigation management.
     * @return
     */
    private static Parent menuForm()
    {
        VBox mainScreen = new VBox(10);
        mainScreen.setAlignment(Pos.CENTER);
        //Title
        Label label = new Label("Tetris");
        label.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        //Buttons
        Button btn = new Button("Click Me");
        Button gameButton = new Button("Play Game");
        Button confButton = new Button("Configurations");
        Button highScoresButton = new Button("High Scores");
        Button exitButton = new Button("Exit");

        //button functionality
        confButton.setOnAction(e -> showConfigScreen());
        gameButton.setOnAction(e -> showGameScreen());
        exitButton.setOnAction(e -> System.exit(0));
        highScoresButton.setOnAction(e -> showHighScoreScreen());

        // sets the button sizes
        gameButton.setPrefWidth(200);
        confButton.setPrefWidth(200);
        exitButton.setPrefWidth(200);
        highScoresButton.setPrefWidth(200);

        // Load a background image??


        //links buttons to screen
        mainScreen.getChildren().addAll(label, gameButton, confButton, highScoresButton, exitButton);
        return mainScreen;
    }

    /**
     * This is also the config design code that used to be existed at show config method.
     * This is also the same reason.
     * @return
     */
    private static VBox buildConfigRoot()
    {
        VBox configScreen = new VBox(10);
        configScreen.setPadding(new Insets(20));
        configScreen.setAlignment(Pos.CENTER);
        Label Title = new Label("CONFIGURATION");
        Title.setMaxWidth(Double.MAX_VALUE);
        Title.setAlignment(Pos.CENTER);

        // Field Width
        Label label = new Label("Field Width: (No of cells):");
        label.setMinWidth(150);
        Slider slider = new Slider(5, 15, 10);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(1);
        slider.setPrefWidth(300);
        HBox labelSliderRow = new HBox(30, label, slider);
        labelSliderRow.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showMainScreen());



        // Field Height
        Label label2 = new Label("Field height: (No of cells):");
        label2.setMinWidth(150);
        Slider slider2 = new Slider(15, 30, 25);
        slider2.setShowTickLabels(true);
        slider2.setShowTickMarks(true);
        slider2.setMajorTickUnit(5);
        slider2.setMinorTickCount(0);
        slider2.setBlockIncrement(1);
        slider2.setPrefWidth(300);
        HBox labelSlider2Row = new HBox(30, label2, slider2);
        labelSlider2Row.setAlignment(Pos.CENTER_LEFT);


        // Game Level
        Label label3 = new Label("Game level:");
        label3.setMinWidth(150);
        Slider slider3 = new Slider(1, 10, 5);
        slider3.setShowTickLabels(true);
        slider3.setShowTickMarks(true);
        slider3.setMajorTickUnit(1);
        slider3.setMinorTickCount(0);
        slider3.setBlockIncrement(1);
        slider3.setPrefWidth(300);
        HBox labelSlider3Row = new HBox(30, label3, slider3);
        labelSlider3Row.setAlignment(Pos.CENTER_LEFT);

        // Music Checkbox
        Label musicLabel = new Label("Music:");
        musicLabel.setMinWidth(150);
        CheckBox checkBox = new CheckBox("Enable Music");
        HBox labelCheckboxRow = new HBox(30, musicLabel, checkBox);
        labelCheckboxRow.setAlignment(Pos.CENTER_LEFT);

        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) {
                System.out.println("on");
            } else {
                System.out.println("off");
            }
        });

        /*sound effect */
        Label soundLabel = new Label("Sound Effect:");
        soundLabel.setMinWidth(150);
        CheckBox checkBox2 = new CheckBox("Enable Sound");
        HBox labelCheckboxRow2 = new HBox(30, soundLabel, checkBox2);
        labelCheckboxRow2.setAlignment(Pos.CENTER_LEFT);

        checkBox2.setOnAction(e -> {
            if (checkBox2.isSelected()) {
                System.out.println("on");
            } else {
                System.out.println("off");
            }
        });

// AI Checkbox
        Label AILabel = new Label("AI Play (on/off):");
        AILabel.setMinWidth(150);
        CheckBox checkBox3 = new CheckBox("Enable AI");
        HBox labelCheckboxRow3 = new HBox(30, AILabel, checkBox3);
        labelCheckboxRow3.setAlignment(Pos.CENTER_LEFT);

// Extend Checkbox
        Label extendLabel = new Label("Extend Mode (on/off):");
        extendLabel.setMinWidth(150);
        CheckBox checkBox4 = new CheckBox("Enable Extend Mode");
        HBox labelCheckboxRow4 = new HBox(30, extendLabel, checkBox4);
        labelCheckboxRow4.setAlignment(Pos.CENTER_LEFT);


        // Add all to root
        configScreen.getChildren().addAll(
                Title,
                labelSliderRow,
                labelSlider2Row,
                labelSlider3Row,
                labelCheckboxRow,
                labelCheckboxRow2,
                labelCheckboxRow3,
                labelCheckboxRow4,
                backButton
        );

        return configScreen;
    }


}




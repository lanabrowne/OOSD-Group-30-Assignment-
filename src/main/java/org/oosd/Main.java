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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

    // Starts application with Splash Screen
    @Override
    public void start(Stage stage) throws Exception {

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
        // using VBox to design of config screen
        VBox config = buildConfigRoot();
        // switch to config screen from menu screen using by root
        scene.setRoot(config);
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
        mainScreen.setAlignment(Pos.CENTER);
        // Title
        Label label = new Label("Tetris");
        label.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

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
        mainScreen.getChildren().addAll(label, gameButton, confButton, highScoresButton, exitButton);
        return mainScreen;
    }

    /**
     * This is also the config design code that used to be existed at show config
     * method.
     * This is also the same reason.
     * 
     * @return
     */
    private static VBox buildConfigRoot() {
        VBox configScreen = new VBox(10);
        configScreen.setPadding(new Insets(20));
        configScreen.setAlignment(Pos.CENTER);
        Label Title = new Label("CONFIGURATION");
        Title.setMaxWidth(Double.MAX_VALUE);
        Title.setAlignment(Pos.CENTER);

        // Field Width
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
        // Label to show current value
        Label widthValueLabel = new Label(String.valueOf((int) slider.getValue()));
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            widthValueLabel.setText(String.valueOf(newVal.intValue()));
        });
        HBox labelSliderRow = new HBox(30, label, slider, widthValueLabel);
        labelSliderRow.setAlignment(Pos.CENTER_LEFT);

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
        // Label to show current value
        Label heightValueLabel = new Label(String.valueOf((int) slider2.getValue()));
        slider2.valueProperty().addListener((obs, oldVal, newVal) -> {
            heightValueLabel.setText(String.valueOf(newVal.intValue()));
        });
        HBox labelSlider2Row = new HBox(30, label2, slider2, heightValueLabel);
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
        // Label to show current value
        Label levelValueLabel = new Label(String.valueOf((int) slider3.getValue()));
        slider3.valueProperty().addListener((obs, oldVal, newVal) -> {
            levelValueLabel.setText(String.valueOf(newVal.intValue()));
        });
        HBox labelSlider3Row = new HBox(30, label3, slider3, levelValueLabel);
        labelSlider3Row.setAlignment(Pos.CENTER_LEFT);

    Label musicLabel = new Label("Music:");
    musicLabel.setMinWidth(150);
    CheckBox checkBox = new CheckBox();
    Label musicStatusLabel = new Label(checkBox.isSelected() ? "Enabled" : "Disabled");
    checkBox.setOnAction(e -> musicStatusLabel.setText(checkBox.isSelected() ? "Enabled" : "Disabled"));
    HBox labelCheckboxRow = new HBox(30, musicLabel, checkBox, musicStatusLabel);
    labelCheckboxRow.setAlignment(Pos.CENTER_LEFT);

    // ----- Sound Effect Checkbox -----
    Label soundLabel = new Label("Sound Effect:");
    soundLabel.setMinWidth(150);
    CheckBox checkBox2 = new CheckBox();
    Label soundStatusLabel = new Label(checkBox2.isSelected() ? "Enabled" : "Disabled");
    checkBox2.setOnAction(e -> soundStatusLabel.setText(checkBox2.isSelected() ? "Enabled" : "Disabled"));
    HBox labelCheckboxRow2 = new HBox(30, soundLabel, checkBox2, soundStatusLabel);
    labelCheckboxRow2.setAlignment(Pos.CENTER_LEFT);

    // ----- AI Checkbox -----
    Label AILabel = new Label("AI Play (on/off):");
    AILabel.setMinWidth(150);
    CheckBox checkBox3 = new CheckBox();
    Label aiStatusLabel = new Label(checkBox3.isSelected() ? "Enabled" : "Disabled");
    checkBox3.setOnAction(e -> aiStatusLabel.setText(checkBox3.isSelected() ? "Enabled" : "Disabled"));
    HBox labelCheckboxRow3 = new HBox(30, AILabel, checkBox3, aiStatusLabel);
    labelCheckboxRow3.setAlignment(Pos.CENTER_LEFT);

    // ----- Extend Mode Checkbox -----
    Label extendLabel = new Label("Extend Mode (on/off):");
    extendLabel.setMinWidth(150);
    CheckBox checkBox4 = new CheckBox();
    Label extendStatusLabel = new Label(checkBox4.isSelected() ? "Enabled" : "Disabled");
    checkBox4.setOnAction(e -> extendStatusLabel.setText(checkBox4.isSelected() ? "Enabled" : "Disabled"));
    HBox labelCheckboxRow4 = new HBox(30, extendLabel, checkBox4, extendStatusLabel);
    labelCheckboxRow4.setAlignment(Pos.CENTER_LEFT);

    // ----- Back Button -----
    Button backButton = new Button("Back");
    backButton.setPrefWidth(200);
    backButton.setOnAction(e -> showMainScreen());

    // Add all to config screen
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

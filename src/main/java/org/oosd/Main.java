package org.oosd;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.oosd.controller.GameController;
import org.oosd.controller.GameScoreController;

import java.io.IOException;


public class Main extends Application {


    //Global Variables
    private StackPane root;
    private Scene scene;
    private final double fieldWidth = 800;
    private final double fieldHeight = 600;
    private AnimationTimer timer;


//    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        root = new StackPane();
        scene = new Scene(root, fieldWidth, fieldHeight);
        // adds title
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tetris");
        // creates the overall box
        primaryStage.show();
        showMainScreen();
    }

    private void showMainScreen() {

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
        root.getChildren().setAll(mainScreen);
    }


    private void showConfigScreen() {
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

        root.getChildren().setAll(configScreen);
    }

    private void showGameScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.oosd/fxml/GameScreen.fxml"));
            Parent game = loader.load();
            scene.setRoot(game);

            Button backButton = new Button("Back");
            backButton.setLayoutX(10);
            backButton.setLayoutY(10);
            backButton.setOnAction(e -> showMainScreen());

            if (game instanceof AnchorPane ap) {
                AnchorPane.setTopAnchor(backButton, 10.0);
                AnchorPane.setLeftAnchor(backButton, 10.0);
                ap.getChildren().add(backButton);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showHighScoreScreen() {
        System.out.println("High score button clicked!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.oosd/fxml/GameScoreScreen.fxml"));
            Parent highScoreRoot = loader.load();
            System.out.println("FXML loaded successfully!");
            scene.setRoot(highScoreRoot);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}




package org.oosd;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;






public class Main extends Application {
    //Global Variables
    private StackPane root;
    private Scene scene;
    private final double fieldWidth = 800;
    private final double fieldHeight = 600;
    private AnimationTimer timer;

    // Block speed
    private double dx = 1;       // X velocity
    private double dy = 1;

    // change block settings
    private String colorString = "RED";
    private boolean hasShadow = false;
    private int size = 40;

    // Edit function later
    private Color getColor(){
        return switch (colorString){
            case "RED" -> Color.RED;
            case "GREEN" -> Color.GREEN;
            case "BLUE" -> Color.BLUE;
            default -> Color.BLACK;

        };
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        root = new StackPane();
        scene = new Scene(root, fieldWidth, fieldHeight);

        showMainScreen();

        // adds title
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tetris");
        // creates the overall box
        primaryStage.show();
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
        confButton.setOnAction(e->showConfigScreen());
        gameButton.setOnAction(e->showGameScreen());
        exitButton.setOnAction(e->System.exit(0));

        // sets the button sizes
        gameButton.setPrefWidth(200);
        confButton.setPrefWidth(200);
        exitButton.setPrefWidth(200);
        highScoresButton.setPrefWidth(200);

        //links buttons to screen
        mainScreen.getChildren().addAll(label, gameButton, confButton, highScoresButton, exitButton);
        root.getChildren().setAll(mainScreen);
    }


    private void showConfigScreen(){
        VBox configScreen = new VBox(10);
        configScreen.setPadding(new Insets(20));
        configScreen.setAlignment(Pos.CENTER);

        //button & label declarations
        Label label = new Label("Configurations");
        label.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        Button backButton = new Button("Back");
        CheckBox cb = new CheckBox("Has Shadow");

        //Radiobutton for block colour
        Label colorLabel = new Label("Color:");
        RadioButton rbRed = new RadioButton("RED");
        RadioButton rbBlue = new RadioButton("BLUE");
        RadioButton rbGreen = new RadioButton("GREEN");
        ToggleGroup group = new ToggleGroup();
        rbBlue.setToggleGroup(group);
        rbBlue.setOnAction(e->colorString="BLUE");
        rbGreen.setToggleGroup(group);
        rbGreen.setOnAction(e->colorString="GREEN");
        rbRed.setToggleGroup(group);
        rbRed.setOnAction(e->colorString="RED");
        switch(colorString){
            case "RED" -> rbRed.setSelected(true);
            case "GREEN" -> rbGreen.setSelected(true);
            default  -> rbBlue.setSelected(true);
        }

        // change block size
        Label sizeLabel = new Label("Size: "+size);
        Slider sizeSlider = new Slider(20,100, size);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setMajorTickUnit(5);
        sizeSlider.valueProperty().addListener(
                (obs,oldVal,newVal) -> {
                    size = newVal.intValue();
                    sizeLabel.setText("Size: "+ size);
                }
        );


        //functionality
        backButton.setOnAction(e->showMainScreen());
        cb.setSelected(hasShadow);
        cb.setOnAction(e->hasShadow = cb.isSelected());

        // adds labels and buttons to screen
        configScreen.getChildren().addAll(label,cb,
                colorLabel,rbBlue,rbGreen,rbRed,
                sizeLabel,sizeSlider,
                backButton);
        root.getChildren().setAll(configScreen);
    }

    private void showGameScreen(){
        Pane gamePane = new Pane();

        // Create field border
        Rectangle field = new Rectangle(0, 0, fieldWidth, fieldHeight);
        field.setFill(Color.TRANSPARENT);
        field.setStroke(Color.BLACK);

        // create tetris block
        Rectangle block = new Rectangle(size, size,getColor());
        block.setX(fieldWidth / 2);
        block.setY(fieldHeight / 2);

        //add back button
        Button backButton = new Button("Back");
        backButton.setLayoutX(10);
        backButton.setLayoutY(10);

        //functionality
        backButton.setOnAction((e -> {
            timer.stop();
            showMainScreen();
        }));

        if(hasShadow){
            DropShadow shadow = new DropShadow();
            shadow.setOffsetX(5);
            shadow.setOffsetY(5);
            block.setEffect(shadow);
        }


        // Key control - copied from lab 4 change later
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) {
                dy = dy > 0?dy+1:dy-1;
            } else if (e.getCode() == KeyCode.DOWN) {
                dy = dy < 0?dy+1:dy-1;
            } else if (e.getCode() == KeyCode.LEFT) {
                dx = dx<0?dx+1:dx-1;
            } else if (e.getCode() == KeyCode.RIGHT) {
                dx = dx>0?dx+1:dx-1;
            }
        });


        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double nextX = block.getX() + dx;
                double nextY = block.getY() + dy;

//                // Bounce off edges - Change later doesn't work
//                if (nextX - block.getX() < 0 || nextX + block.getY() > fieldWidth) {
//                    dx = -dx;
//                }
//                if (nextY - block.getX() < 0 || nextY + block.getY() > fieldHeight) {
//                    dy = -dy;
//                }

                block.setX(block.getX() + dx);
                block.setY(block.getY() + dy);
            }
        };

        timer.start();



        gamePane.getChildren().addAll(field, block, backButton);
        root.getChildren().setAll(gamePane);
        gamePane.requestFocus();  // Ensure pane gets key input
    }
}


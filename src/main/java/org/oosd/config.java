package org.oosd;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class config extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(40);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(30, 30, 30, 30));

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
        root.getChildren().addAll(
                Title,
                labelSliderRow,
                labelSlider2Row,
                labelSlider3Row,
                labelCheckboxRow,
                labelCheckboxRow2,
                labelCheckboxRow3,
                labelCheckboxRow4


        );

        Scene scene = new Scene(root, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Configuration");
        primaryStage.show();
    }
}

package org.oosd.config;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.oosd.Main;

import java.util.Optional;

public class TetrisConfigView {

    public static Parent buildConfigRoot(Runnable backAction)
    {

        //Read JSON file to collect Default value first
        ConfigService.load();
        TetrisConfig config = ConfigService.get();




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
        //Here will be user input so set config to collect user input value and sent to JSON
        Slider slider = new Slider(5, 15, config.fieldWidth());
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
        //Here is the user input of field height. So collect it and store into JSON file
        Slider slider2 = new Slider(15, 30, config.fieldHeight());
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
        Slider slider3 = new Slider(1, 10, config.gameLevel());
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
        //Validate user input below command and collect check or not
        checkBox.setSelected(config.music());
        Label musicStatusLabel = new Label(checkBox.isSelected() ? "Enabled" : "Disabled");
        checkBox.setOnAction(e -> musicStatusLabel.setText(checkBox.isSelected() ? "Enabled" : "Disabled"));
        HBox labelCheckboxRow = new HBox(30, musicLabel, checkBox, musicStatusLabel);
        labelCheckboxRow.setAlignment(Pos.CENTER_LEFT);

        // ----- Sound Effect Checkbox -----
        Label soundLabel = new Label("Sound Effect:");
        soundLabel.setMinWidth(150);
        CheckBox checkBox2 = new CheckBox();
        //Check user input true or false and store into record type of sfx
        checkBox2.setSelected(config.sfx());
        Label soundStatusLabel = new Label(checkBox2.isSelected() ? "Enabled" : "Disabled");
        checkBox2.setOnAction(e -> soundStatusLabel.setText(checkBox2.isSelected() ? "Enabled" : "Disabled"));
        HBox labelCheckboxRow2 = new HBox(30, soundLabel, checkBox2, soundStatusLabel);
        labelCheckboxRow2.setAlignment(Pos.CENTER_LEFT);

        // ----- AI Checkbox -----
        Label AILabel = new Label("AI Play (on/off):");
        AILabel.setMinWidth(150);
        CheckBox checkBox3 = new CheckBox();
        //Here is AI play check box so collect user selection and send to record type class
        checkBox3.setSelected(config.aiPlay());
        Label aiStatusLabel = new Label(checkBox3.isSelected() ? "Enabled" : "Disabled");
        checkBox3.setOnAction(e -> aiStatusLabel.setText(checkBox3.isSelected() ? "Enabled" : "Disabled"));
        HBox labelCheckboxRow3 = new HBox(30, AILabel, checkBox3, aiStatusLabel);
        labelCheckboxRow3.setAlignment(Pos.CENTER_LEFT);

        // ----- Extend Mode Checkbox -----
        Label extendLabel = new Label("Extend Mode (on/off):");
        extendLabel.setMinWidth(150);
        CheckBox checkBox4 = new CheckBox();
        //Here is extend mode selection by check box. So get user input and send to Tetris config class
        checkBox4.setSelected(config.extendMode());
        Label extendStatusLabel = new Label(checkBox4.isSelected() ? "Enabled" : "Disabled");
        checkBox4.setOnAction(e -> extendStatusLabel.setText(checkBox4.isSelected() ? "Enabled" : "Disabled"));
        HBox labelCheckboxRow4 = new HBox(30, extendLabel, checkBox4, extendStatusLabel);
        labelCheckboxRow4.setAlignment(Pos.CENTER_LEFT);

        //Create save button to save current user config condition
        Button btnSave  = new Button("Save");
        btnSave.setPrefWidth(200);
        btnSave.setOnAction(e -> {
            //send current UI value into Tetris Config
            TetrisConfig newConfig = new TetrisConfig(
                    (int) slider.getValue(),
                    (int) slider2.getValue(),
                    (int) slider3.getValue(),
                    checkBox.isSelected(),
                    checkBox2.isSelected(),
                    checkBox3.isSelected(),
                    checkBox4.isSelected()
            );
            ConfigService.update(newConfig);
            saveNotification();

        });

        // ----- Back Button -----
        Button backButton = new Button("Back");
        backButton.setPrefWidth(200);
        backButton.setOnAction(e -> {
            if (backAction != null) backAction.run();
        });

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
                new HBox(12, btnSave, backButton)
        );

        return configScreen;
    }


    public static boolean saveNotification() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Notification");
        alert.setHeaderText("Notification");
        alert.setContentText("Your Changes Recorded");

        ButtonType yesButton = new ButtonType("Close", ButtonBar.ButtonData.YES);
        alert.getButtonTypes().setAll(yesButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }
}

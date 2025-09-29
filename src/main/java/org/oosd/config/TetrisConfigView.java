package org.oosd.config;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.Optional;

public class TetrisConfigView {

    public static Parent buildConfigRoot(Runnable backAction, Runnable singlePlayer, Runnable twoPlayer, Runnable twoPlayerAI) {
        // Load current config
        ConfigService.load();
        TetrisConfig config = ConfigService.get();

        VBox configScreen = new VBox(15);
        configScreen.setPadding(new Insets(20));
        configScreen.setAlignment(Pos.CENTER);

        Label title = new Label("CONFIGURATION");
        title.setFont(Font.font(24));
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        // --- Sliders ---
        Slider sliderWidth = new Slider(5, 15, config.fieldWidth());
        Label widthValueLabel = new Label(String.valueOf((int) sliderWidth.getValue()));
        sliderWidth.valueProperty().addListener((obs, oldVal, newVal) -> widthValueLabel.setText(String.valueOf(newVal.intValue())));
        HBox widthRow = new HBox(10, new Label("Field Width:"), sliderWidth, widthValueLabel);
        widthRow.setAlignment(Pos.CENTER_LEFT);

        Slider sliderHeight = new Slider(10, 25, config.fieldHeight());
        Label heightValueLabel = new Label(String.valueOf((int) sliderHeight.getValue()));
        sliderHeight.valueProperty().addListener((obs, oldVal, newVal) -> heightValueLabel.setText(String.valueOf(newVal.intValue())));
        HBox heightRow = new HBox(10, new Label("Field Height:"), sliderHeight, heightValueLabel);
        heightRow.setAlignment(Pos.CENTER_LEFT);

        Slider sliderLevel = new Slider(1, 10, config.gameLevel());
        Label levelValueLabel = new Label(String.valueOf((int) sliderLevel.getValue()));
        sliderLevel.valueProperty().addListener((obs, oldVal, newVal) -> levelValueLabel.setText(String.valueOf(newVal.intValue())));
        HBox levelRow = new HBox(10, new Label("Game Level:"), sliderLevel, levelValueLabel);
        levelRow.setAlignment(Pos.CENTER_LEFT);

        // --- Checkboxes ---
        CheckBox musicCheckBox = new CheckBox();
        musicCheckBox.setSelected(config.music());
        HBox musicRow = new HBox(10, new Label("Music:"), musicCheckBox);
        musicRow.setAlignment(Pos.CENTER_LEFT);

        CheckBox sfxCheckBox = new CheckBox();
        sfxCheckBox.setSelected(config.sfx());
        HBox sfxRow = new HBox(10, new Label("Sound Effect:"), sfxCheckBox);
        sfxRow.setAlignment(Pos.CENTER_LEFT);

        CheckBox aiCheckBox = new CheckBox();
        aiCheckBox.setSelected(config.aiPlay());
        HBox aiRow = new HBox(10, new Label("AI Play:"), aiCheckBox);
        aiRow.setAlignment(Pos.CENTER_LEFT);

        // --- Extended Mode & Two-Player ---
        CheckBox extendedCheckBox = new CheckBox("Extended Mode");
        extendedCheckBox.setSelected(config.extendMode());

        // Player 1
        Label player1Label = new Label("Player One Type:");
        RadioButton player1Human = new RadioButton("Human");
        RadioButton player1AI = new RadioButton("AI");
        ToggleGroup player1Group = new ToggleGroup();
        player1Human.setToggleGroup(player1Group);
        player1AI.setToggleGroup(player1Group);
        player1Human.setSelected(true);

        // Player 2
        Label player2Label = new Label("Player Two Type:");
        RadioButton player2Human = new RadioButton("Human");
        RadioButton player2AI = new RadioButton("AI");
        ToggleGroup player2Group = new ToggleGroup();
        player2Human.setToggleGroup(player2Group);
        player2AI.setToggleGroup(player2Group);
        player2Human.setSelected(true);

        // Player 2 disabled until Extended Mode is checked
        player2Human.setDisable(!extendedCheckBox.isSelected());
        player2AI.setDisable(!extendedCheckBox.isSelected());
        extendedCheckBox.setOnAction(ev -> {
            boolean extended = extendedCheckBox.isSelected();
            player2Human.setDisable(!extended);
            player2AI.setDisable(!extended);
        });

        VBox playerBox = new VBox(5,
                extendedCheckBox,
                player1Label, player1Human, player1AI,
                player2Label, player2Human, player2AI
        );

        // --- Buttons ---
        Button playButton = new Button("Play");
playButton.setPrefWidth(120);
playButton.setOnAction(ev -> {
    boolean extended = extendedCheckBox.isSelected();
    boolean p1Human = player1Human.isSelected();
    boolean p2Human = player2Human.isSelected();

    if (extended && !p1Human && !p2Human) {
        // AI vs AI selected → call AI callback
        twoPlayerAI.run();
    } else if (extended && p1Human && p2Human) {
        twoPlayer.run();   // normal two-player
    } else {
        singlePlayer.run(); // single-player
    }
});


        Button backButton = new Button("Back");
        backButton.setPrefWidth(120);
        backButton.setOnAction(e -> backAction.run());

        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(120);
        saveButton.setOnAction(e -> {
            TetrisConfig newConfig = new TetrisConfig(
                    (int) sliderWidth.getValue(),
                    (int) sliderHeight.getValue(),
                    (int) sliderLevel.getValue(),
                    musicCheckBox.isSelected(),
                    sfxCheckBox.isSelected(),
                    aiCheckBox.isSelected(),
                    extendedCheckBox.isSelected()
            );
            ConfigService.update(newConfig);
            saveNotification();
        });

        HBox buttonBox = new HBox(10, saveButton, backButton, playButton);
        buttonBox.setAlignment(Pos.CENTER);

        // --- Assemble Config Screen ---
        configScreen.getChildren().addAll(
                title,
                widthRow,
                heightRow,
                levelRow,
                musicRow,
                sfxRow,
                aiRow,
                playerBox,
                buttonBox
        );

        return configScreen;
    }

    // --- Save Notification ---
    public static boolean saveNotification() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Notification");
        alert.setHeaderText("Notification");
        alert.setContentText("Your Changes Recorded");

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(closeButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == closeButton;
    }
}

package org.oosd.config;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.oosd.audio.audioManager;

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
        musicCheckBox.setSelected(audioManager.getInstance().isMusicEnabled());
        HBox musicRow = new HBox(10, new Label("Music:"), musicCheckBox);
        musicRow.setAlignment(Pos.CENTER_LEFT);

        CheckBox sfxCheckBox = new CheckBox();
        sfxCheckBox.setSelected(audioManager.getInstance().isSfxEnabled());
        HBox sfxRow = new HBox(10, new Label("Sound Effect:"), sfxCheckBox);
        sfxRow.setAlignment(Pos.CENTER_LEFT);

        // Sync checkboxes with a instantly
        musicCheckBox.setOnAction(e -> audioManager.getInstance().toggleMusic());
        sfxCheckBox.setOnAction(e -> audioManager.getInstance().toggleSFX());

        CheckBox aiCheckBox = new CheckBox();
        aiCheckBox.setSelected(config.aiPlay());
        HBox aiRow = new HBox(10, new Label("AI Play:"), aiCheckBox);
        aiRow.setAlignment(Pos.CENTER_LEFT);

        CheckBox extendedCheckBox = new CheckBox("Extended Mode");
        extendedCheckBox.setSelected(config.extendMode());
        HBox extendRow = new HBox(10, new Label("Extended Mode:"), extendedCheckBox);
        extendRow.setAlignment(Pos.CENTER_LEFT);

        // Player selection
        Label player1Label = new Label("Player One Type:");
        RadioButton player1Human = new RadioButton("Human");
        RadioButton player1AI = new RadioButton("AI");
        RadioButton player1External = new RadioButton("External");

        ToggleGroup player1Group = new ToggleGroup();
        player1Human.setToggleGroup(player1Group);
        player1AI.setToggleGroup(player1Group);
        player1External.setToggleGroup(player1Group);
        player1Human.setSelected(true);

        Label player2Label = new Label("Player Two Type:");
        RadioButton player2Human = new RadioButton("Human");
        RadioButton player2AI = new RadioButton("AI");
        RadioButton player2External = new RadioButton("External");

        ToggleGroup player2Group = new ToggleGroup();
        player2Human.setToggleGroup(player2Group);
        player2AI.setToggleGroup(player2Group);
        player2External.setToggleGroup(player2Group);
        player2Human.setSelected(true);

        VBox playerBox = new VBox(5,
                player1Label, player1Human, player1AI, player1External,
                player2Label, player2Human, player2AI, player2External
        );
        playerBox.setVisible(extendedCheckBox.isSelected());
        playerBox.setManaged(extendedCheckBox.isSelected());

        extendedCheckBox.setOnAction(event -> {
            boolean extended = extendedCheckBox.isSelected();
            playerBox.setVisible(extended);
            playerBox.setManaged(extended);
        });

        Button backButton = new Button("Back");
        backButton.setPrefWidth(120);
        backButton.setOnAction(e -> backAction.run());

        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(120);
        saveButton.setOnAction(e -> {
            PlayerType left;
            if (player1Human.isSelected()) {
                left = PlayerType.HUMAN;
            } else if (player1AI.isSelected()) {
                left = PlayerType.AI;
            } else {
                left = PlayerType.EXTERNAL;
            }

            PlayerType right;
            if (!extendedCheckBox.isSelected()) {
                right = PlayerType.HUMAN;
            } else {
                if (player2Human.isSelected()) {
                    right = PlayerType.HUMAN;
                } else if (player2AI.isSelected()) {
                    right = PlayerType.AI;
                } else {
                    right = PlayerType.EXTERNAL;
                }
            }

            TetrisConfig newConfig = new TetrisConfig(
                    (int) sliderWidth.getValue(),
                    (int) sliderHeight.getValue(),
                    (int) sliderLevel.getValue(),
                    musicCheckBox.isSelected(),
                    sfxCheckBox.isSelected(),
                    aiCheckBox.isSelected(),
                    left,
                    right,
                    extendedCheckBox.isSelected()
            );
            ConfigService.update(newConfig);

        });

        HBox buttonBox = new HBox(10, saveButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        configScreen.getChildren().addAll(
                title,
                widthRow,
                heightRow,
                levelRow,
                musicRow,
                sfxRow,
                aiRow,
                extendRow,
                playerBox,
                buttonBox
        );

        return configScreen;
    }

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

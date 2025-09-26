package org.oosd.HighScore;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.oosd.Main;

public class GameScoreController {

    private Main main;
    public void setMain(Main main) { this.main = main; }

    @FXML private TableView<PlayerScore> scoreTable;
    @FXML private TableColumn<PlayerScore, Number> rankColumn;
    @FXML private TableColumn<PlayerScore, String> nameColumn;
    @FXML private TableColumn<PlayerScore, Number> scoreColumn;
    @FXML private TableColumn<PlayerScore, String> configColumn;
    @FXML private Button clearButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        ScoreStore.loadFromJsonResource("/org/oosd/HighScore/JavaTetrisScore.json");

        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        scoreColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getScore()));
        configColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getConfig()));

        rankColumn.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(scoreTable.getItems().indexOf(cd.getValue()) + 1)
        );

        scoreTable.setItems(ScoreStore.getScores());
        scoreTable.setPlaceholder(new Label("No scores yet"));

        clearButton.setOnAction(e -> {
            ScoreStore.clear();
            scoreTable.refresh();

            ScoreStore.loadFromJsonResource("/org/oosd/HighScore/JavaTetrisScore.json");
            scoreTable.setItems(ScoreStore.getScores());
            scoreTable.refresh();
        });

        if (backButton != null) {
            backButton.setOnAction(e -> {
                if (main != null) {
                    main.showScreen(main.getMainScreen());
                } else {
                    System.out.println("Back pressed (main not set)");
                }
            });
        }
    }
}


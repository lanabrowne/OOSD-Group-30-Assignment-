package org.oosd.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.oosd.HighScore.PlayerScore;
import org.oosd.HighScore.ScoreStore;
import org.oosd.HighScore.HighScoreWriter;
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

    private static final String DASH = "—";

    @FXML
    public void initialize() {
        // Ensure writable file and load Top10 (padded with placeholders)
        HighScoreWriter.ensureFile();
        ScoreStore.loadFromJsonFile(HighScoreWriter.SCORE_PATH);

        // Name
        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        nameColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                PlayerScore ps = empty ? null : getTableRow().getItem();
                setText((ps == null || ps.isPlaceholder()) ? DASH : item);
            }
        });

        // Score
        scoreColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getScore()));
        scoreColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                PlayerScore ps = empty ? null : getTableRow().getItem();
                setText((ps == null || ps.isPlaceholder()) ? DASH : String.valueOf(item.intValue()));
            }
        });

        // Config (use the config snapshot saved per entry)
        configColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getConfig()));
        configColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                PlayerScore ps = empty ? null : getTableRow().getItem();
                setText((ps == null || ps.isPlaceholder()) ? DASH : item);
            }
        });

        // Rank
        rankColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) { setText(null); return; }
                PlayerScore ps = getTableRow().getItem();
                setText((ps == null || ps.isPlaceholder()) ? DASH : String.valueOf(getIndex() + 1));
            }
        });

        // Bind data
        scoreTable.setItems(ScoreStore.getScores());

        // Clear button -> truncate file to []
        clearButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clear Confirmation");
            alert.setHeaderText("Clear High Scores");
            alert.setContentText("Are you sure you want to clear all scores?");
            ButtonType no  = new ButtonType("No",  ButtonBar.ButtonData.NO);
            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            alert.getButtonTypes().setAll(no, yes);

            alert.showAndWait().ifPresent(bt -> {
                if (bt == yes) {
                    HighScoreWriter.clearFile(); // write "[]"
                    ScoreStore.loadFromJsonFile(HighScoreWriter.SCORE_PATH); // reload
                    scoreTable.setItems(ScoreStore.getScores());
                    scoreTable.refresh();
                }
            });
        });

        // Back button
        if (backButton != null) {
            backButton.setOnAction(e -> {
                if (main != null) main.showScreen(main.getMainScreen());
            });
        }

        // Fix table height (10 rows)
        scoreTable.setFixedCellSize(28);
        scoreTable.prefHeightProperty().bind(
                scoreTable.fixedCellSizeProperty().multiply(10 + 1.01)
        );
        scoreTable.minHeightProperty().bind(scoreTable.prefHeightProperty());
        scoreTable.maxHeightProperty().bind(scoreTable.prefHeightProperty());
    }
}
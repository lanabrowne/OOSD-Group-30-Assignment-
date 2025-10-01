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

    private static final String DASH = "—";
    private static final String JSON_PATH = "/org/oosd/HighScore/JavaTetrisScore.json";

    @FXML
    public void initialize() {
        ScoreStore.loadFromJsonResource(JSON_PATH);

        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        nameColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                PlayerScore ps = empty ? null : getTableRow().getItem();
                setText((ps == null || ps.isPlaceholder()) ? DASH : item);
            }
        });

        scoreColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getScore()));
        scoreColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                PlayerScore ps = empty ? null : getTableRow().getItem();
                setText((ps == null || ps.isPlaceholder()) ? DASH : String.valueOf(item.intValue()));
            }
        });

        configColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getConfig()));
        configColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                PlayerScore ps = empty ? null : getTableRow().getItem();
                setText((ps == null || ps.isPlaceholder()) ? DASH : item);
            }
        });

        rankColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) { setText(null); return; }
                PlayerScore ps = getTableRow().getItem();
                setText((ps == null || ps.isPlaceholder()) ? DASH : String.valueOf(getIndex() + 1));
            }
        });

        scoreTable.setItems(ScoreStore.getScores());

        clearButton.setOnAction(e -> {
            ScoreStore.clear();
            scoreTable.refresh();
        });

        if (backButton != null) {
            backButton.setOnAction(e -> {
                if (main != null) main.showScreen(main.getMainScreen());
            });
        }
    }
}

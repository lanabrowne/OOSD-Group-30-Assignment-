package org.oosd.HighScore;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class GameScoreController {

    @FXML private TableView<PlayerScore> scoreTable;
    @FXML private TableColumn<PlayerScore, Number> rankColumn;
    @FXML private TableColumn<PlayerScore, String> nameColumn;
    @FXML private TableColumn<PlayerScore, Number> scoreColumn;
    @FXML private TableColumn<PlayerScore, String> configColumn;
    @FXML private Button clearButton;
    @FXML private Button backButton; // ダミー（今は使わない）

    @FXML
    public void initialize() {
        // リソースのJSONを読み込む（表示専用）
        ScoreStore.loadFromJsonResource("/org.oosd/HighScore/JavaTetrisScore.json");

        nameColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getName()));
        scoreColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getScore()));
        configColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getConfig()));

        // ランク列（1〜）
        rankColumn.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(scoreTable.getItems().indexOf(cd.getValue()) + 1)
        );

        scoreTable.setItems(ScoreStore.getScores());
        scoreTable.setPlaceholder(new Label("No scores yet"));

        clearButton.setOnAction(e -> {
            // 表示リストを空にする（JSONは変更しない）
            ScoreStore.clear();
            scoreTable.refresh();

            // ※ すぐ元に戻したい場合は下を使ってね（任意）
            // ScoreStore.loadFromJsonResource("/org.oosd/HighScore/JavaTetrisScore.json");
            // scoreTable.setItems(ScoreStore.getScores());
            // scoreTable.refresh();
        });

        if (backButton != null) {
            backButton.setOnAction(e -> System.out.println("Back pressed (not wired)"));
        }
    }
}

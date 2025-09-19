package org.oosd.HighScore;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.util.Objects;

public class HighScoreApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var url = Objects.requireNonNull(
                getClass().getResource("/org/oosd/HighScore/GameScoreScreen.fxml"),
                "FXML not found: /org.oosd/HighScore/GameScoreScreen.fxml"
        );
        Parent root = FXMLLoader.load(url);
        stage.setTitle("High Score");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

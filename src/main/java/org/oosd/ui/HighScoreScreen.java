package org.oosd.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.oosd.Main;
import org.oosd.HighScore.GameScoreController; // ★ Controllerの実パッケージに合わせる

import java.net.URL;
import java.util.Objects;

public class HighScoreScreen implements Screen {
    private final Main main;
    public HighScoreScreen(Main main) { this.main = main; }

    @Override
    public Parent getScreen() {
        try {
            URL url = Objects.requireNonNull(
                    getClass().getResource("/org/oosd/HighScore/GameScoreScreen.fxml"),
                    "FXML not found: /org/oosd/HighScore/GameScoreScreen.fxml"
            );

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            GameScoreController controller = loader.getController();
            if (controller != null) {
                controller.setMain(main);
            }

            return root;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load HighScore FXML", e);
        }
    }

    @Override
    public void setRoute(String path, Screen screen) { /* not used */ }
}

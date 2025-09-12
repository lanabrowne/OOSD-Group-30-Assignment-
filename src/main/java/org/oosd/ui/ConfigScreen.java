package org.oosd.ui;
import org.oosd.Main;
import javafx.scene.Parent;
import org.oosd.config.TetrisConfigView;

public class ConfigScreen implements Screen {
        private Main main;

        public ConfigScreen(Main main) {
            this.main = main;
        }

        @Override
        public Parent getScreen() {
            TetrisConfigView view = new TetrisConfigView();
            // Use a lambda to go back to the main menu
            return view.buildConfigRoot(() -> main.showScreen(main.getMainScreen()));
        }
        @Override
        public void setRoute(String path, Screen screen){
            // ConfigScreen does not need routes, so this can be empty
        }
    }


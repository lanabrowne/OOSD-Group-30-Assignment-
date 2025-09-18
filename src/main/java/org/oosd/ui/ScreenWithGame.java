package org.oosd.ui;

import org.oosd.controller.GameController;

public interface ScreenWithGame extends Screen {
    //Inject the controller instead of a separate class
    void initializeGameController(GameController controller);
}

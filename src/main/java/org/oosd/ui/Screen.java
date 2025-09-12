package org.oosd.ui;

import javafx.scene.Node;
import javafx.scene.Parent;

public interface Screen {
    Parent getScreen();
    void setRoute(String path, Screen screen);
}

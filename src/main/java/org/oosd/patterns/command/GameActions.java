package org.oosd.patterns.command;

public interface GameActions {
    void moveLeft();
    void moveRight();
    void rotateCW();
    void softDrop();
    void hardDrop();
    void togglePause();
}
package org.oosd.ui;

public interface Frame {
    public void showScreen(Screen scr);

    public void showExitConfirmation();
    MainScreen getMainScreen(); 
}

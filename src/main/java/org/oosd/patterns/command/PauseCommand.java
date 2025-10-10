package org.oosd.patterns.command;

public class PauseCommand implements Command {
    private final GameActions actions;
    public PauseCommand(GameActions actions) { this.actions = actions; }
    @Override public void execute() { actions.togglePause(); }
}

package org.oosd.patterns.command;

public class RotateCommand implements Command {
    private final GameActions actions;
    public RotateCommand(GameActions actions) { this.actions = actions; }
    @Override public void execute() { actions.rotateCW(); }
}

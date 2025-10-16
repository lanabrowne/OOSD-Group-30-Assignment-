package org.oosd.patterns.command;

public class LeftCommand implements Command {
    private final GameActions actions;
    public LeftCommand(GameActions actions) { this.actions = actions; }
    @Override public void execute() { actions.moveLeft(); }
}

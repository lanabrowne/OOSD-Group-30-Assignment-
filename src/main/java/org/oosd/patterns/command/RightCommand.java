package org.oosd.patterns.command;

public class RightCommand implements Command {
    private final GameActions actions;
    public RightCommand(GameActions actions) { this.actions = actions; }
    @Override public void execute() { actions.moveRight(); }
}

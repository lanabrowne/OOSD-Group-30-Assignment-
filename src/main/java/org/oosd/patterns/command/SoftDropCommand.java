package org.oosd.patterns.command;

public class SoftDropCommand implements Command {
    private final GameActions actions;
    public SoftDropCommand(GameActions actions) { this.actions = actions; }
    @Override public void execute() { actions.softDrop(); }
}

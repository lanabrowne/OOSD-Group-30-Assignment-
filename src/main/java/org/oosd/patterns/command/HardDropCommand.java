package org.oosd.patterns.command;

public class HardDropCommand implements Command {
    private final GameActions actions;
    public HardDropCommand(GameActions actions) { this.actions = actions; }
    @Override public void execute() { actions.hardDrop(); }
}

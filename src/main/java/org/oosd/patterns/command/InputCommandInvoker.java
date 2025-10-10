package org.oosd.patterns.command;

import java.util.HashMap;
import java.util.Map;

public class InputCommandInvoker {
    private final Map<String, Command> commands = new HashMap<>();

    public InputCommandInvoker register(String name, Command cmd) {
        commands.put(name.toUpperCase(), cmd);
        return this;
    }

    public void execute(String name) {
        Command c = commands.get(name.toUpperCase());
        if (c != null) c.execute();
    }

    /** Optional: convenience defaults */
    public static InputCommandInvoker withDefaults(GameActions actions) {
        return new InputCommandInvoker()
                .register("LEFT",   () -> actions.moveLeft())
                .register("RIGHT",  () -> actions.moveRight())
                .register("ROTATE", () -> actions.rotateCW())
                .register("DOWN",   () -> actions.softDrop())
                .register("HARD_DROP", () -> actions.hardDrop())
                .register("PAUSE",  () -> actions.togglePause());
    }
}


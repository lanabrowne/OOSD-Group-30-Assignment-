package org.oosd.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record OpMove(int opX, int opRotate) {

    /**
     * Returns a human-readable string version of this move,
     * mainly for debugging and logging.
     */
    @Override
    public String toString() {
        return "OpMove{" +
                "opX=" + opX +
                ", opRotate=" + opRotate +
                '}';
    }

    /**
     * Converts the move into a simple text-based action list.
     * This can be used by ExternalPlayer or GameController to apply moves in order.
     */
    public String[] toActions() {
        if (opX == 0 && opRotate == 0) {
            return new String[]{"DOWN"};
        }

        java.util.List<String> actions = new java.util.ArrayList<>();

        // Add rotations
        for (int i = 0; i < opRotate; i++) {
            actions.add("ROTATE");
        }

        // Move left or right based on opX value
        if (opX < 0) {
            for (int i = 0; i < Math.abs(opX); i++) {
                actions.add("LEFT");
            }
        } else if (opX > 0) {
            for (int i = 0; i < opX; i++) {
                actions.add("RIGHT");
            }
        }

        // Always move down at the end
        actions.add("DOWN");

        return actions.toArray(new String[0]);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OpMove other = (OpMove) obj;
        return this.opX == other.opX && this.opRotate == other.opRotate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(opX, opRotate);
    }
}

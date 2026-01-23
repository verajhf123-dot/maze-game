package de.tum.cit.fop.maze;

/**
 * Represents the exit of a level.
 * The class only stores whether the player has reached the exit.
 */

public class Exit {

    private boolean reached = false;

    public void onPlayerReach() {
        reached = true;
    }

    public boolean isReached() {
        return reached;
    }
}

package de.tum.cit.fop.maze;

public class Exit {

    private boolean reached = false;

    public void onPlayerReach() {
        reached = true;
    }

    public boolean isReached() {
        return reached;
    }
}
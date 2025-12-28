package de.tum.cit.fop.maze;

public class Door {

    private boolean open = false;

    public boolean isOpen() {
        return open;
    }


    public void tryOpen(Player player) {
        if (!open && player.getStats().hasKey()) {
            player.getStats().useKey();
            open = true;
        }
    }
}
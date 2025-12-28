package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;
public class Key {
    private Rectangle bounds;
    private boolean collected = false;

    public Key(float x, float y) {
        bounds = new Rectangle(x, y, 32, 32);
    }

    public void checkPickup(Player player) {
        if (!collected && bounds.overlaps(player.getHitbox())) {
            collected = true;
            player.getStats().obtainKey();
            System.out.println("Key collected!");
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}

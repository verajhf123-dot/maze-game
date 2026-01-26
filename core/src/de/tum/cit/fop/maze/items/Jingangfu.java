package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;

public class Jingangfu extends Item {

    // Constructor: creates a Jingangfu item at given position
    public Jingangfu(float x, float y) {
        super(
                x,
                y,
                32f,                 // Item size
                "items_jingangfu.png", // Texture path
                Color.GOLD           // Fallback color
        );
    }

    // Called when an entity picks up the item
    @Override
    public void onPickup(Object entity) {
        // Only players can pick up this item
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;

        // Enable fatal damage protection for the player
        player.enableFatalProtection();
    }
}
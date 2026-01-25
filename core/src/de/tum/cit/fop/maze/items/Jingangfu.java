package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;

/**
 * Jingangfu item.
 * Grants the player a temporary fatal damage protection when picked up.
 */
public class Jingangfu extends Item {

    /**
     * Constructor for Jingangfu item.
     * @param x X position in the world
     * @param y Y position in the world
     */
    public Jingangfu(float x, float y) {
        // Call the parent constructor with size, texture path and fallback color
        super(
                x,
                y,
                32f,                    // width & height
                "items_jingangfu.png",  // texture path
                Color.GOLD              // fallback color if texture is missing
        );

        // Debug print to verify creation
        System.out.println("Jingangfu created at (" + x + "," + y + ")");
    }

    /**
     * Called when an entity picks up this item.
     * Grants the player fatal damage protection.
     * @param entity The entity that picked up this item
     */
    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;

        // Enable fatal protection on the player
        player.enableFatalProtection();

        // Debug print for pickup
        System.out.println("Player picked up Jingangfu, fatal protection enabled.");
    }
}
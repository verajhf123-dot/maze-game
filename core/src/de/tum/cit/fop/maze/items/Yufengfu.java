package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;

/**
 * Yufengfu item.
 * Grants the player a temporary speed boost when picked up.
 */
public class Yufengfu extends Item {

    /**
     * Constructor for Yufengfu.
     * @param x X position in the world
     * @param y Y position in the world
     */
    public Yufengfu(float x, float y) {
        super(
                x,
                y,
                32f,                    // size of the item
                "item_Yufengfu.png",    // texture path
                Color.CYAN              // fallback color
        );

        System.out.println("Yufengfu created at (" + x + "," + y + ")");
    }

    /**
     * Called when an entity picks up this item.
     * Grants the player a speed buff for 20 seconds.
     * @param entity The entity that picked up this item
     */
    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;

        // Apply a 20-second speed buff with 1.2x multiplier
        player.applySpeedBuff(1.2f, 20f);

        System.out.println("Yufengfu picked up: speed buff applied to player");
    }
}
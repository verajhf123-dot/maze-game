package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.PlayerStats;

/**
 * Xiandan item.
 * Restores player's health or grants experience if already full.
 */
public class Xiandan extends Item {

    /**
     * Constructor for Xiandan.
     * @param x X position in the world
     * @param y Y position in the world
     */
    public Xiandan(float x, float y) {
        super(
                x,
                y,
                32f,                    // size
                "items_Xiandan.png",    // texture path
                Color.GREEN             // fallback color
        );

        System.out.println("Xiandan created at (" + x + "," + y + ")");
    }

    /**
     * Called when an entity picks up this item.
     * Heals the player or grants experience if health is full.
     * @param entity The entity that picked up this item
     */
    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        PlayerStats stats = player.getStats();

        if (stats != null) {
            // Check if player is already at full health
            if (player.getHealth() >= player.getMaxHealth()) {
                stats.getExpSystem().gainExp(50); // grant experience points
                System.out.println("Player at full health. XP +50");
            } else {
                player.healByPercentage(0.3f); // heal 30% of max health
                System.out.println("Xiandan used: healed player by 30%");
            }
        }
    }
}
package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.PlayerStats;

public class Xiandan extends Item {

    // Constructor: creates a Xiandan item at the given position
    public Xiandan(float x, float y) {
        super(
                x,
                y,
                32f,
                "items_Xiandan.png",
                Color.GREEN
        );
    }

    // Called when an entity picks up the item
    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        PlayerStats stats = player.getStats();

        if (stats != null) {

            // If player health is full, grant experience instead
            if (player.getHealth() >= player.getMaxHealth()) {

                System.out.println("(XP +50)");

                stats.getExpSystem().gainExp(50);
            } else {
                // Otherwise, heal the player by a percentage of max health
                System.out.println("get Xiandan");

                player.healByPercentage(0.3f);
            }
        }
    }
}
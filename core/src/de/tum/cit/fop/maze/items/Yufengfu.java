package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;

public class Yufengfu extends Item {

    // Constructor: creates a Yufengfu item at the given position
    public Yufengfu(float x, float y) {
        super(
                x,
                y,
                32f,
                "item_Yufengfu.png",
                Color.CYAN
        );
    }

    // Called when an entity picks up the item
    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        // Apply a temporary movement speed buff to the player
        Player player = (Player) entity;
        player.applySpeedBuff(1.2f, 20f);
    }
}
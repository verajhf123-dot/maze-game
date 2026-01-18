package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;

public class Jingangfu extends Item {

    public Jingangfu(float x, float y) {
        super(
                x,
                y,
                32f,
                "items_jingangfu.png",
                Color.GOLD
        );
    }

    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        player.enableFatalProtection();
    }
}

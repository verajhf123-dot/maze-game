package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;

public class Yufengfu extends Item {

    public Yufengfu(float x, float y) {
        super(
                x,
                y,
                16f,
                "item_Yufengfu.png",
                Color.CYAN
        );
    }

    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        player.applySpeedBuff(1.2f, 20f);
    }
}


package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;

public class Xiandan extends Item {

    public Xiandan(float x, float y) {
        super(
                x,
                y,
                16f,
                "items/xiandan.png", // 有就用，没有就自动忽略
                Color.GREEN
        );
    }

    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        player.healByPercentage(0.2f);
    }
}


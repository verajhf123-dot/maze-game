package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.PlayerStats;

public class Xiandan extends Item {

    public Xiandan(float x, float y) {
        super(
                x,
                y,
                16f,
                "items_Xiandan.png", // 有就用，没有就自动忽略
                Color.GREEN
        );
    }

    @Override
    public void onPickup(Object entity) {
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        PlayerStats stats = player.getStats();

        if (stats != null) {

            if (player.getHealth() >= player.getMaxHealth()) {

                System.out.println("(XP +50)");

                stats.getExpSystem().gainExp(50);
            } else {
                System.out.println("get Xiandan");

                player.healByPercentage(0.3f);
            }
        }
    }
}

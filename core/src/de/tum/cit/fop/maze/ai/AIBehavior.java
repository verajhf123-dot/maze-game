package de.tum.cit.fop.maze.ai;

import de.tum.cit.fop.maze.enemies.Enemy;

public interface AIBehavior {
    void update(Enemy enemy, de.tum.cit.fop.maze.Player player, float deltaTime);
    String getBehaviorName();
}
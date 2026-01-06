package de.tum.cit.fop.maze.ai;

import de.tum.cit.fop.maze.enemies.Enemy;

public abstract class AIBehavior {
    protected Enemy enemy;
    protected float updateInterval = 0.2f;
    protected float updateTimer = 0;

    public AIBehavior(Enemy enemy) {
        this.enemy = enemy;
    }

    public void update(float delta) {
        updateTimer += delta;
        if (updateTimer >= updateInterval) {
            updateAI();
            updateTimer = 0;
        }
    }

    protected abstract void updateAI();

    public abstract void onPlayerSpotted();
    public abstract void onPlayerLost();
    public abstract void onAttack();
}
package de.tum.cit.fop.maze.ai;

import de.tum.cit.fop.maze.enemies.Enemy;

public abstract class AIBehavior {

    protected Enemy enemy;

    // how often the AI should update (in seconds)
    protected float updateInterval = 0.2f;
    protected float timer = 0f;

    public AIBehavior(Enemy enemy) {
        this.enemy = enemy;
    }

    public void update(float delta) {
        timer += delta;

        if (timer < updateInterval) {
            return;
        }

        updateAI();
        timer = 0f;
    }

    protected abstract void updateAI();

    // called when the player enters the enemy's detection range
    public abstract void onPlayerSpotted();

    // called when the player leaves the detection range
    public abstract void onPlayerLost();

    public abstract void onAttack();
}

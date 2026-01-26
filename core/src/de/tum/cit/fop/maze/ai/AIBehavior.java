package de.tum.cit.fop.maze.ai;

import de.tum.cit.fop.maze.enemies.Enemy;

/**
 * Abstract base class for enemy AI behavior.
 * Provides a common update mechanism and AI event interfaces.
 */
public abstract class AIBehavior {

    /** Enemy controlled by this AI behavior. */
    protected Enemy enemy;

    /** Time interval (in seconds) between AI updates. */
    protected float updateInterval = 0.2f;

    /** Time elapsed since the last AI update. */
    protected float updateTimer = 0;

    /**
     * Creates an AI behavior for the given enemy.
     *
     * @param enemy the controlled enemy
     */
    public AIBehavior(Enemy enemy) {
        this.enemy = enemy;
    }

    /**
     * Updates the AI. The core logic is executed
     * only when the update interval has elapsed.
     *
     * @param delta time since last frame (in seconds)
     */
    public void update(float delta) {
        updateTimer += delta;
        if (updateTimer >= updateInterval) {
            updateAI();
            updateTimer = 0;
        }
    }

    /** Executes the AI-specific logic. */
    protected abstract void updateAI();

    /** Called when the player is detected. */
    public abstract void onPlayerSpotted();

    /** Called when the player is no longer detected. */
    public abstract void onPlayerLost();

    /** Called when the enemy attacks. */
    public abstract void onAttack();
}
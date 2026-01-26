package de.tum.cit.fop.maze.ai;

import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.Player;
import com.badlogic.gdx.math.Vector2;

/**
 * AI behavior that makes an enemy retreat when its health is low.
 * The enemy moves away from the player for a limited duration
 * and then stops retreating.
 */
public class RetreatBehavior extends AIBehavior {

    /** Health ratio threshold to trigger retreat. */
    private float retreatThreshold = 0.3f;

    /** Speed multiplier applied during retreat. */
    private float retreatSpeedMultiplier = 1.5f;

    /** Duration of retreat in update steps. */
    private int retreatDuration = 120;

    /** Remaining retreat steps. */
    private int retreatTimer = 0;

    /** Indicates whether the enemy is currently retreating. */
    private boolean isRetreating = false;

    /** Player that the enemy retreats from. */
    private Player targetPlayer;

    /**
     * Creates a retreat behavior for the given enemy.
     *
     * @param enemy the enemy using this behavior
     */
    public RetreatBehavior(Enemy enemy) {
        super(enemy);
    }

    /**
     * Updates the retreat logic.
     * Retreats from the player if health falls below threshold
     * and handles movement and duration.
     */
    @Override
    protected void updateAI() {
        if (targetPlayer == null) return;

        float healthRatio = (float) enemy.getHealth() / enemy.getMaxHealth();

        if (!isRetreating && healthRatio < retreatThreshold) {
            startRetreat();
        }

        if (isRetreating) {
            if (retreatTimer > 0) {
                Vector2 enemyPos = enemy.getPosition();
                Vector2 playerPos = targetPlayer.getPosition();

                float dx = enemyPos.x - playerPos.x;
                float dy = enemyPos.y - playerPos.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance > 0) {
                    dx /= distance;
                    dy /= distance;

                    float speed = enemy.getSpeed() * retreatSpeedMultiplier;
                    enemy.setVelocity(dx * speed, dy * speed);
                }

                retreatTimer--;

                if (retreatTimer <= 0) {
                    stopRetreat();
                }
            }
        }
    }

    /**
     * Starts the retreat behavior and initializes the timer.
     */
    private void startRetreat() {
        isRetreating = true;
        retreatTimer = retreatDuration;
        System.out.println("Enemy starts retreating! Health: " + enemy.getHealth() + "/" + enemy.getMaxHealth());
    }

    /**
     * Stops the retreat behavior and resets movement.
     */
    private void stopRetreat() {
        isRetreating = false;
        enemy.setVelocity(0, 0);
        System.out.println("Enemy stops retreating");
    }

    @Override
    public void onPlayerSpotted() {
        // Behavior triggered when player is detected
        System.out.println("Player spotted - retreat behavior activated");
    }

    @Override
    public void onPlayerLost() {
        if (isRetreating) {
            stopRetreat();
        }
        targetPlayer = null;
        System.out.println("Player lost - stop retreating");
    }

    @Override
    public void onAttack() {
        // No special handling required
        System.out.println("Retreat behavior: enemy is attacking");
    }

    /**
     * Sets the player that triggers this retreat behavior.
     *
     * @param player the target player
     */
    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
    }
}
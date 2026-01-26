package de.tum.cit.fop.maze.ai;

import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.Player;
import com.badlogic.gdx.math.Vector2;

/**
 * AI behavior that allows an enemy to evade the player.
 * When the player is within a certain distance, the enemy
 * moves away for a limited duration and then enters a cooldown.
 */
public class EvadeBehavior extends AIBehavior {

    /** Distance threshold for triggering evasion. */
    private float evadeDistance = 150f;

    /** Speed multiplier applied during evasion. */
    private float evadeSpeedMultiplier = 1.8f;

    /** Cooldown time (in update steps) between evades. */
    private int evadeCooldown = 60;

    /** Remaining cooldown steps. */
    private int cooldownTimer = 0;

    /** Indicates whether the enemy is currently evading. */
    private boolean isEvading = false;

    /** Duration of the evasion (in update steps). */
    private int evadeDuration = 30;

    /** Remaining evasion steps. */
    private int evadeTimer = 0;

    /** Player that triggers the evasion behavior. */
    private Player targetPlayer;

    /**
     * Creates an evade behavior for the given enemy.
     *
     * @param enemy the enemy using this behavior
     */
    public EvadeBehavior(Enemy enemy) {
        super(enemy);
    }

    /**
     * Updates the evasion logic.
     * Starts evasion when the player is close enough
     * and handles movement and cooldown timing.
     */
    @Override
    protected void updateAI() {
        if (targetPlayer == null) return;

        if (cooldownTimer > 0) {
            cooldownTimer--;
        }

        float distanceToPlayer = calculateDistance();

        if (!isEvading &&
                cooldownTimer <= 0 &&
                distanceToPlayer < evadeDistance) {

            startEvade();
        }

        if (isEvading) {
            if (evadeTimer > 0) {
                Vector2 enemyPos = enemy.getPosition();
                Vector2 playerPos = targetPlayer.getPosition();

                float dx = enemyPos.x - playerPos.x;
                float dy = enemyPos.y - playerPos.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance > 0) {
                    dx /= distance;
                    dy /= distance;

                    float speed = enemy.getSpeed() * evadeSpeedMultiplier;
                    enemy.setVelocity(dx * speed, dy * speed);
                }

                evadeTimer--;

                if (evadeTimer <= 0) {
                    stopEvade();
                }
            }
        }
    }

    /**
     * Starts the evasion behavior and initializes timers.
     */
    private void startEvade() {
        isEvading = true;
        evadeTimer = evadeDuration;
        cooldownTimer = evadeCooldown;

        System.out.println("Enemy evades attack! Distance: " + calculateDistance());
    }

    /**
     * Stops the evasion behavior and resets movement.
     */
    private void stopEvade() {
        isEvading = false;
        enemy.setVelocity(0, 0);
    }

    /**
     * Calculates the distance between the enemy and the player.
     *
     * @return distance to the target player
     */
    private float calculateDistance() {
        if (targetPlayer == null) return Float.MAX_VALUE;

        Vector2 enemyPos = enemy.getPosition();
        Vector2 playerPos = targetPlayer.getPosition();

        float dx = enemyPos.x - playerPos.x;
        float dy = enemyPos.y - playerPos.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void onPlayerSpotted() {
        // Evade behavior is enabled once a player is detected
        System.out.println("Player spotted - evade behavior activated");
    }

    @Override
    public void onPlayerLost() {
        if (isEvading) {
            stopEvade();
        }
        targetPlayer = null;
        System.out.println("Player lost - stop evading");
    }

    @Override
    public void onAttack() {
        // No special handling required
        System.out.println("Evade behavior: enemy preparing to attack");
    }

    /**
     * Sets the player that triggers this evasion behavior.
     *
     * @param player the target player
     */
    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
    }
}
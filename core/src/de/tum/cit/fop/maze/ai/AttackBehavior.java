package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.Player;

/**
 * AI behavior that handles attacking the player.
 * The enemy moves towards the player and attacks
 * when within attack range.
 */
public class AttackBehavior extends AIBehavior {

    /** Reference to the player. */
    private Player player;

    /** Time between two consecutive attacks (in seconds). */
    private float attackCooldown = 1f;

    /** Remaining cooldown time until the next attack. */
    private float currentCooldown = 0;

    /**
     * Creates an attack behavior for the given enemy and player.
     *
     * @param enemy the attacking enemy
     * @param player the target player
     */
    public AttackBehavior(Enemy enemy, Player player) {
        super(enemy);
        this.player = player;
    }

    /**
     * Updates the attack logic.
     * Moves towards the player and performs an attack
     * when the player is within range and the cooldown
     * has expired.
     */
    @Override
    protected void updateAI() {
        if (currentCooldown > 0) {
            currentCooldown -= updateInterval;
        }

        Vector2 playerPos = player.getPosition();
        Vector2 enemyPos = enemy.getPosition();
        float distance = enemyPos.dst(playerPos);

        if (distance <= enemy.getAttackRange()) {
            enemy.setVelocity(0, 0);

            if (currentCooldown <= 0) {
                performAttack();
                currentCooldown = attackCooldown;
            }
        } else {
            enemy.findPathTo(playerPos);
        }
    }

    /**
     * Executes the attack action.
     */
    private void performAttack() {
        enemy.attack();
    }

    @Override
    public void onPlayerSpotted() {
        // No additional behavior required
    }

    @Override
    public void onPlayerLost() {
        // No additional behavior required
    }

    @Override
    public void onAttack() {
        // No additional behavior required
    }
}
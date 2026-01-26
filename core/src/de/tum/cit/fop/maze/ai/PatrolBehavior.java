package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.enemies.Enemy;

/**
 * AI behavior that makes an enemy patrol randomly
 * around its current position.
 */
public class PatrolBehavior extends AIBehavior {

    /** Current patrol target position. */
    private Vector2 patrolPoint;

    /** Maximum distance of patrol points from the origin position. */
    private float patrolRadius = 100f;

    /** Time the enemy remains idle at a patrol point (in seconds). */
    private float idleTime = 2f;

    /** Accumulated idle time. */
    private float currentIdleTime = 0;

    /** Indicates whether the enemy is currently idle. */
    private boolean isIdle = false;

    /**
     * Creates a patrol behavior for the given enemy.
     *
     * @param enemy the patrolling enemy
     */
    public PatrolBehavior(Enemy enemy) {
        super(enemy);
        generateNewPatrolPoint();
    }

    /**
     * Updates the patrol logic.
     * The enemy moves towards a patrol point and
     * waits for a short time once it is reached.
     */
    @Override
    protected void updateAI() {
        if (isIdle) {
            currentIdleTime += updateInterval;
            if (currentIdleTime >= idleTime) {
                isIdle = false;
                generateNewPatrolPoint();
            }
            return;
        }

        Vector2 currentPos = enemy.getPosition();
        float distance = currentPos.dst(patrolPoint);

        if (distance < 10f) {
            isIdle = true;
            currentIdleTime = 0;
            enemy.setVelocity(0, 0);
        } else {
            Vector2 direction = new Vector2(
                    patrolPoint.x - currentPos.x,
                    patrolPoint.y - currentPos.y
            ).nor();
            enemy.setVelocity(
                    direction.x * enemy.getSpeed(),
                    direction.y * enemy.getSpeed()
            );
        }
    }

    /**
     * Generates a new random patrol point within the patrol radius.
     */
    private void generateNewPatrolPoint() {
        Vector2 currentPos = enemy.getPosition();
        float angle = MathUtils.random(0, 360) * MathUtils.degreesToRadians;

        patrolPoint = new Vector2(
                currentPos.x + MathUtils.cos(angle) * patrolRadius,
                currentPos.y + MathUtils.sin(angle) * patrolRadius
        );
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
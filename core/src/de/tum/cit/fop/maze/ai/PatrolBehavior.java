package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.enemies.Enemy;

public class PatrolBehavior extends AIBehavior {

    private Vector2 targetPoint;

    private float radius = 100f;

    private float waitTime = 2f;
    private float waitTimer = 0f;
    private boolean waiting = false;

    public PatrolBehavior(Enemy enemy) {
        super(enemy);
        pickNewPoint();
    }

    @Override
    protected void updateAI() {
        if (waiting) {
            waitTimer += updateInterval;
            if (waitTimer >= waitTime) {
                waiting = false;
                pickNewPoint();
            }
            return;
        }

        Vector2 pos = enemy.getPosition();
        float dist = pos.dst(targetPoint);

        if (dist < 10f) {
            waiting = true;
            waitTimer = 0f;
            enemy.setVelocity(0f, 0f);
            return;
        }

        Vector2 dir = new Vector2(
                targetPoint.x - pos.x,
                targetPoint.y - pos.y
        ).nor();

        enemy.setVelocity(
                dir.x * enemy.getSpeed(),
                dir.y * enemy.getSpeed()
        );
    }

    private void pickNewPoint() {
        Vector2 pos = enemy.getPosition();

        float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;

        targetPoint = new Vector2(
                pos.x + MathUtils.cos(angle) * radius,
                pos.y + MathUtils.sin(angle) * radius
        );
    }

    @Override
    public void onPlayerSpotted() {
        // patrol ignores player
    }

    @Override
    public void onPlayerLost() {
        // nothing to do
    }

    @Override
    public void onAttack() {
        // not used
    }
}
package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.enemies.Enemy;

public class PatrolBehavior extends AIBehavior {
    private Vector2 patrolPoint;
    private float patrolRadius = 100f;
    private float idleTime = 2f;
    private float currentIdleTime = 0;
    private boolean isIdle = false;

    public PatrolBehavior(Enemy enemy) {
        super(enemy);
        generateNewPatrolPoint();
    }

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
            Vector2 direction = new Vector2(patrolPoint.x - currentPos.x,
                    patrolPoint.y - currentPos.y).nor();
            enemy.setVelocity(direction.x * enemy.getSpeed(), direction.y * enemy.getSpeed());
        }
    }

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
        // 切换到追击行为
    }

    @Override
    public void onPlayerLost() {
        // 返回巡逻
    }

    @Override
    public void onAttack() {
        // 攻击行为
    }
}

package de.tum.cit.fop.maze.ai;

import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.Player;
import com.badlogic.gdx.math.Vector2;

public class RetreatBehavior extends AIBehavior {
    private float retreatThreshold = 0.3f;
    private float retreatSpeedMultiplier = 1.5f;
    private int retreatDuration = 120;
    private int retreatTimer = 0;
    private boolean isRetreating = false;
    private Player targetPlayer;

    public RetreatBehavior(Enemy enemy) {
        super(enemy);
    }

    @Override
    protected void updateAI() {
        if (targetPlayer == null) return;

        float healthRatio = enemy.getHealth() / enemy.getMaxHealth();

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

    private void startRetreat() {
        isRetreating = true;
        retreatTimer = retreatDuration;
        // enemy.setState("RETREATING");
        System.out.println("Enemy starts retreating! Health: " + enemy.getHealth() + "/" + enemy.getMaxHealth());
    }

    private void stopRetreat() {
        isRetreating = false;
        // enemy.setState("PATROLLING");
        enemy.setVelocity(0, 0);
        System.out.println("Enemy stops retreating");
    }

    @Override
    public void onPlayerSpotted() {
        // this.targetPlayer = enemy.getTargetPlayer();
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
        System.out.println("Retreat behavior: enemy is attacking");
    }

    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
    }
}
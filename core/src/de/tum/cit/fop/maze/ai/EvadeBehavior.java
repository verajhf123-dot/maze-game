package de.tum.cit.fop.maze.ai;

import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.Player;
import com.badlogic.gdx.math.Vector2;

public class EvadeBehavior extends AIBehavior {
    private float evadeDistance = 150f;
    private float evadeSpeedMultiplier = 1.8f;
    private int evadeCooldown = 60;
    private int cooldownTimer = 0;
    private boolean isEvading = false;
    private int evadeDuration = 30;
    private int evadeTimer = 0;
    private Player targetPlayer;

    public EvadeBehavior(Enemy enemy) {
        super(enemy);
    }

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

    private void startEvade() {
        isEvading = true;
        evadeTimer = evadeDuration;
        cooldownTimer = evadeCooldown;

        System.out.println("Enemy evades attack! Distance: " + calculateDistance());
    }

    private void stopEvade() {
        isEvading = false;
        enemy.setVelocity(0, 0);
    }

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
        System.out.println("Evade behavior: enemy preparing to attack");
    }

    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
    }
}

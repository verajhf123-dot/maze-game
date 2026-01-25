package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.enemies.Enemy;

public class EvadeBehavior extends AIBehavior {

    private float evadeRange = 150f;
    private float speedMultiplier = 1.8f;

    private int evadeCooldown = 60;
    private int cooldown = 0;

    private boolean evading = false;
    private int evadeTime = 30;
    private int evadeLeft = 0;

    private Player player;

    public EvadeBehavior(Enemy enemy) {
        super(enemy);
    }

    @Override
    protected void updateAI() {
        if (player == null) return;

        if (cooldown > 0) {
            cooldown--;
        }

        float dist = distanceToPlayer();

        if (!evading && cooldown <= 0 && dist < evadeRange) {
            startEvade();
        }

        if (!evading) return;

        if (evadeLeft > 0) {
            Vector2 enemyPos = enemy.getPosition();
            Vector2 playerPos = player.getPosition();

            float dx = enemyPos.x - playerPos.x;
            float dy = enemyPos.y - playerPos.y;
            float len = (float) Math.sqrt(dx * dx + dy * dy);

            if (len > 0f) {
                dx /= len;
                dy /= len;

                float speed = enemy.getSpeed() * speedMultiplier;
                enemy.setVelocity(dx * speed, dy * speed);
            }

            evadeLeft--;

            if (evadeLeft <= 0) {
                stopEvade();
            }
        }
    }

    private void startEvade() {
        evading = true;
        evadeLeft = evadeTime;
        cooldown = evadeCooldown;

        System.out.println("Enemy evades! dist=" + distanceToPlayer());
    }

    private void stopEvade() {
        evading = false;
        enemy.setVelocity(0f, 0f);
    }

    private float distanceToPlayer() {
        Vector2 enemyPos = enemy.getPosition();
        Vector2 playerPos = player.getPosition();

        float dx = enemyPos.x - playerPos.x;
        float dy = enemyPos.y - playerPos.y;

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void onPlayerSpotted() {
        System.out.println("Player spotted, enemy ready to evade");
    }

    @Override
    public void onPlayerLost() {
        if (evading) {
            stopEvade();
        }
        player = null;
        System.out.println("Player lost");
    }

    @Override
    public void onAttack() {
        // nothing special here
    }

    public void setTargetPlayer(Player player) {
        this.player = player;
    }
}
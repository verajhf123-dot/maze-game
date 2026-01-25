package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.enemies.Enemy;

public class RetreatBehavior extends AIBehavior {

    private float healthLimit = 0.3f;
    private float speedMultiplier = 1.5f;

    private int retreatTime = 120;
    private int timeLeft = 0;

    private boolean retreating = false;

    private Player player;

    public RetreatBehavior(Enemy enemy) {
        super(enemy);
    }

    @Override
    protected void updateAI() {
        if (player == null) return;

        float hpRatio = (float) enemy.getHealth() / enemy.getMaxHealth();

        if (!retreating && hpRatio < healthLimit) {
            startRetreat();
        }

        if (!retreating) return;

        if (timeLeft > 0) {
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

            timeLeft--;

            if (timeLeft <= 0) {
                stopRetreat();
            }
        }
    }

    private void startRetreat() {
        retreating = true;
        timeLeft = retreatTime;

        System.out.println(
                "Enemy retreating (" +
                        enemy.getHealth() + "/" + enemy.getMaxHealth() + ")"
        );
    }

    private void stopRetreat() {
        retreating = false;
        enemy.setVelocity(0f, 0f);
        System.out.println("Enemy stopped retreating");
    }

    @Override
    public void onPlayerSpotted() {
        System.out.println("Player spotted, retreat behavior active");
    }

    @Override
    public void onPlayerLost() {
        if (retreating) {
            stopRetreat();
        }
        player = null;
        System.out.println("Player lost");
    }

    @Override
    public void onAttack() {
        // retreat does not react to attacks
    }

    public void setTargetPlayer(Player player) {
        this.player = player;
    }
}
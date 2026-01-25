package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.enemies.Enemy;

public class AttackBehavior extends AIBehavior {

    private Player player;

    private float cooldown = 1f;
    private float cooldownTimer = 0f;

    public AttackBehavior(Enemy enemy, Player player) {
        super(enemy);
        this.player = player;
    }

    @Override
    protected void updateAI() {
        if (cooldownTimer > 0f) {
            cooldownTimer -= updateInterval;
        }

        Vector2 enemyPos = enemy.getPosition();
        Vector2 playerPos = player.getPosition();

        float dist = enemyPos.dst(playerPos);

        if (dist <= enemy.getAttackRange()) {
            enemy.setVelocity(0f, 0f);

            if (cooldownTimer <= 0f) {
                enemy.attack();
                cooldownTimer = cooldown;
            }
        } else {
            enemy.findPathTo(playerPos);
        }
    }

    @Override
    public void onPlayerSpotted() {
        // not needed for this behavior
    }

    @Override
    public void onPlayerLost() {
        // not needed for this behavior
    }

    @Override
    public void onAttack() {
        // handled in updateAI
    }
}
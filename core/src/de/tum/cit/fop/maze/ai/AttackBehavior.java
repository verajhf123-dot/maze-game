package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.enemies.Enemy;
import de.tum.cit.fop.maze.Player;

public class AttackBehavior extends AIBehavior {
    private Player player;
    private float attackCooldown = 1f;
    private float currentCooldown = 0;

    public AttackBehavior(Enemy enemy, Player player) {
        super(enemy);
        this.player = player;
    }

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

    private void performAttack() {
        enemy.attack();
    }

    @Override
    public void onPlayerSpotted() {
    }

    @Override
    public void onPlayerLost() {
    }

    @Override
    public void onAttack() {
    }
}

package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.ai.AIBehavior;
import de.tum.cit.fop.maze.ai.AStarPathFinder;
import de.tum.cit.fop.maze.Player; // 添加这行导入
import java.util.List;

public abstract class Enemy {
    protected Vector2 position;
    protected Vector2 velocity;
    protected Rectangle bounds;
    protected float speed;
    protected float health;
    protected float maxHealth;
    protected float attackDamage;
    protected float attackRange;
    protected float detectionRange;

    protected AIBehavior currentBehavior;
    protected AStarPathFinder pathFinder;
    protected List<Vector2> currentPath;
    protected int currentPathIndex;

    public Enemy(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        this.bounds = new Rectangle(x, y, width, height);
        this.currentPathIndex = 0;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void clearPath() {
        currentPath = null;
        currentPathIndex = 0;
        velocity.set(0, 0); // 停止移动
    }

    public void setWalkableGrid(boolean[][] grid) {
        // 这个方法可能不再需要，因为我们使用 PathFinder
        // 但为了兼容性保留
    }

    // 注意：这里有两个不同的 attack 方法
    // 1. 这个接收 Player 参数
    public void attack(Player player) {
        if (player != null) {
            // 检查 Player 类是否有 takeDamage 方法
            // 如果没有，你需要创建它
            player.takeDamage(attackDamage);
        }
    }

    // 2. 这个是抽象方法，由子类实现
    public abstract void attack();

    // 删除重复的方法定义（第59-63行）
    // public float getDetectionRange() {
    //     return detectionRange;
    // }
    //
    // public float getAttackRange() {
    //     return attackRange;
    // }

    public void setTargetPosition(Vector2 target) {
        findPathTo(target);
    }

    public void update(float delta) {
        if (!isAlive()) return;

        // 更新位置
        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);

        // 更新AI行为
        if (currentBehavior != null) {
            currentBehavior.update(delta);
        }

        // 沿路径移动
        if (currentPath != null && !currentPath.isEmpty()) {
            followPath(delta);
        }

        // 边界检查
        keepInBounds();
    }

    private void followPath(float delta) {
        if (currentPathIndex >= currentPath.size()) {
            currentPath = null;
            currentPathIndex = 0;
            velocity.set(0, 0);
            return;
        }

        Vector2 target = currentPath.get(currentPathIndex);
        Vector2 direction = new Vector2(target.x - position.x, target.y - position.y);

        if (direction.len() < 5f) {
            currentPathIndex++;
        } else {
            direction.nor();
            velocity.set(direction.x * speed, direction.y * speed);
        }
    }

    private void keepInBounds() {
        // 确保敌人在地图边界内
        if (position.x < 0) position.x = 0;
        if (position.y < 0) position.y = 0;
        if (position.x > 800 - bounds.width) position.x = 800 - bounds.width;
        if (position.y > 600 - bounds.height) position.y = 600 - bounds.height;
    }

    public void setPathFinder(AStarPathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    public void findPathTo(Vector2 target) {
        if (pathFinder != null) {
            currentPath = pathFinder.findPath(position, target);
            currentPathIndex = 0;
        }
    }

    public void setBehavior(AIBehavior behavior) {
        this.currentBehavior = behavior;
    }

    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            onDeath();
        }
    }

    public abstract void render(SpriteBatch batch);
    protected abstract void onDeath();

    // Getters - 这些已经在下文定义了，不要重复
    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }
    public float getSpeed() { return speed; }
    public float getHealth() { return health; }
    public float getAttackRange() { return attackRange; }
    public float getDetectionRange() { return detectionRange; }
    public void setVelocity(float x, float y) { velocity.set(x, y); }
}
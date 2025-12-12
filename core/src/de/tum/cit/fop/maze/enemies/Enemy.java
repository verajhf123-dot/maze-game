package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Enemy {
    protected Vector2 position;
    protected Rectangle bounds;
    protected boolean isAlive = true;
    protected Vector2 targetPosition;
    protected boolean[][] walkableGrid;

    public Enemy(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, width, height);
        this.targetPosition = new Vector2();
    }

    // ===== 以下是 GameScreen 需要的方法 =====

    public abstract void update(float delta);

    public abstract void render(SpriteBatch batch);

    public void attack(Object target) {
        // 子类实现
    }

    public boolean isAlive() {
        return isAlive;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        bounds.setPosition(position.x, position.y);
        return bounds;
    }

    public void setTargetPosition(Vector2 target) {
        this.targetPosition = target;
    }

    public void setWalkableGrid(boolean[][] grid) {
        this.walkableGrid = grid;
    }

    // ===== 其他可能需要的方法 =====

    public void takeDamage(float damage) {
        // 子类实现
    }
}
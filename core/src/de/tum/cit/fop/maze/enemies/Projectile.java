package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.GameScreen;

public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private Rectangle bounds;
    private float speed;
    private float damage;
    private boolean active;
    private Texture texture;
    private Color color;
    private float lifetime = 3f;
    private float currentLifetime = 0f;

    public Projectile(float x, float y, float directionX, float directionY,
                      float speed, float damage, Color color) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);
        this.bounds = new Rectangle(x, y, 8, 8);
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.color = color;
    }

    public Projectile(float x, float y, float directionX, float directionY, float speed, float damage, Texture texture, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);

        // 现在这里的 width 和 height 就能对应上参数里的数值了
        this.bounds = new Rectangle(x, y, width, height);

        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.texture = texture;
        this.color = Color.WHITE;
    }

    public Projectile(float x, float y, Vector2 direction, float speed, float damage, Color color) {
        this(x, y, direction.x, direction.y, speed, damage, color);
    }

    public void update(float delta) {
        if (!active) return;

        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);

        currentLifetime += delta;
        if (currentLifetime >= lifetime) {
            active = false;
        }

        if (position.x < 0 || position.x > 800 || position.y < 0 || position.y > 600) {
            active = false;
        }
    }

    // 修改 Projectile.java 中的 render 方法
    public void render(SpriteBatch batch) {
        if (!active) return;

        if (texture != null) {
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        } else {
            // 如果没有贴图，我们可以尝试画一个调试用的矩形（如果您的项目中有单像素贴图的话）
            // 或者，至少打印一条错误日志，告诉自己这里出问题了
            System.err.println("Projectile texture is NULL! Position: " + position);
        }
    }

    public boolean checkPlayerHit(Player player) {
        if (!active) return false;

        if (bounds.overlaps(player.getHitbox())) {
            player.takeDamage(damage);
            active = false;
            return true;
        }
        return false;
    }

    public boolean isActive() { return active; }
    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    public boolean checkEnemyHit(Array<Enemy> enemies) {
        if (!active) return false;
        boolean hasHit = false;

        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(damage);
                hasHit = true;

                return true;
            }
        }
        return false;
    }
}

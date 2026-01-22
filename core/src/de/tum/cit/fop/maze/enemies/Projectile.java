package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.Animation; // ✅ 新增引用
import com.badlogic.gdx.graphics.g2d.TextureRegion; // ✅ 新增引用
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.Player;

public class Projectile {
    private Vector2 position;
    private Vector2 velocity;
    private Rectangle bounds;
    private float speed;
    private float damage;
    private boolean active;

    private Texture texture; // 静态图
    private Animation<TextureRegion> animation; // 🔥 新增：动画支持
    private float stateTime = 0f; // 🔥 新增：动画播放计时器

    private Color color;
    private float lifetime = 3f;
    private float currentLifetime = 0f;

    // 构造函数1：纯颜色
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

    // 构造函数2：静态图片 (Texture) - 用于闪电等不需要动的图
    public Projectile(float x, float y, float directionX, float directionY, float speed, float damage, Texture texture, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);
        this.bounds = new Rectangle(x, y, width, height);
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.texture = texture;
        this.color = Color.WHITE;
    }

    // 🔥 构造函数3：动画 (Animation) - 专门用于火球 🔥
    public Projectile(float x, float y, float directionX, float directionY, float speed, float damage, Animation<TextureRegion> animation, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);
        this.bounds = new Rectangle(x, y, width, height); // 设置碰撞箱大小
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.animation = animation; // 保存动画
        this.color = Color.WHITE;
    }

    public Projectile(float x, float y, Vector2 direction, float speed, float damage, Color color) {
        this(x, y, direction.x, direction.y, speed, damage, color);
    }

    public void update(float delta) {
        if (!active) return;

        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);

        // 🔥 更新动画时间
        stateTime += delta;

        currentLifetime += delta;
        if (currentLifetime >= lifetime) {
            active = false;
        }

        // 简单的边界检查 (假设地图大概 3000x3000，防止飞太远不消失)
        if (position.x < -200 || position.x > 3000 || position.y < -200 || position.y > 3000) {
            active = false;
        }
    }

    // 修改 Projectile.java 中的 render 方法
    public void render(SpriteBatch batch) {
        if (!active) return;

        // 🔥 优先画动画
        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, position.x, position.y, bounds.width, bounds.height);
        }
        // 其次画静态图
        else if (texture != null) {
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        }
        else {
            // 都没有贴图时，打印错误 (可选：可以画一个 debug 红块)
            // System.err.println("Projectile texture is NULL! Position: " + position);
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
        // Animation 不需要 dispose，因为它引用的 TextureRegion 是由 GameScreen 管理的
    }

    public boolean checkEnemyHit(Array<Enemy> enemies) {
        if (!active) return false;

        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(damage);
                return true;
            }
        }
        return false;
    }
}
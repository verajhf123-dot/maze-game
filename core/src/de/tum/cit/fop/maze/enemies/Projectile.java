package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    private Texture texture;
    private Animation<TextureRegion> animation;
    private float stateTime = 0f;

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
        this.bounds = new Rectangle(x, y, width, height);
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.texture = texture;
        this.color = Color.WHITE;
    }

    public Projectile(float x, float y, float directionX, float directionY, float speed, float damage, Animation<TextureRegion> animation, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);
        this.bounds = new Rectangle(x, y, width, height);
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.animation = animation;
        this.color = Color.WHITE;
    }

    public Projectile(float x, float y, Vector2 direction, float speed, float damage, Color color) {
        this(x, y, direction.x, direction.y, speed, damage, color);
    }

    public void update(float delta) {
        if (!active) return;

        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);

        stateTime += delta;

        currentLifetime += delta;
        if (currentLifetime >= lifetime) {
            active = false;
        }

        if (position.x < -200 || position.x > 3000 || position.y < -200 || position.y > 3000) {
            active = false;
        }
    }


    public void render(SpriteBatch batch) {
        if (!active) return;

        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, position.x, position.y, bounds.width, bounds.height);
        }
        else if (texture != null) {
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        }
        else {
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

        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(damage);
                return true;
            }
        }
        return false;
    }
}
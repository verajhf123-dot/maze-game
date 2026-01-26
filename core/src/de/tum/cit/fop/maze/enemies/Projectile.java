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

/**
 * Represents a projectile fired by enemies or players.
 * Handles movement, rendering, collision detection, and lifetime.
 */
public class Projectile {
    private Vector2 position;           // Current position
    private Vector2 velocity;           // Movement vector
    private Rectangle bounds;           // Collision box
    private float speed;                // Movement speed
    private float damage;               // Damage dealt on hit
    private boolean active;             // Active state

    private Texture texture;            // Optional texture
    private Animation<TextureRegion> animation; // Optional animation
    private float stateTime = 0f;       // Time for animation

    private Color color;                // Projectile color
    private float lifetime = 3f;        // Maximum lifetime
    private float currentLifetime = 0f; // Current elapsed time

    /** Constructor using direction vector and color. */
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

    /** Constructor with texture and size. */
    public Projectile(float x, float y, float directionX, float directionY,
                      float speed, float damage, Texture texture, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);
        this.bounds = new Rectangle(x, y, width, height);
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.texture = texture;
        this.color = Color.WHITE;
    }

    /** Constructor with animation and size. */
    public Projectile(float x, float y, float directionX, float directionY,
                      float speed, float damage, Animation<TextureRegion> animation, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);
        this.bounds = new Rectangle(x, y, width, height);
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.animation = animation;
        this.color = Color.WHITE;
    }

    /** Alternative constructor using a Vector2 direction. */
    public Projectile(float x, float y, Vector2 direction, float speed, float damage, Color color) {
        this(x, y, direction.x, direction.y, speed, damage, color);
    }

    /** Updates position, lifetime, and deactivates if out of bounds. */
    public void update(float delta) {
        if (!active) return;

        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);

        stateTime += delta;
        currentLifetime += delta;

        if (currentLifetime >= lifetime) active = false;
        if (position.x < -200 || position.x > 3000 || position.y < -200 || position.y > 3000)
            active = false;
    }

    /** Renders the projectile using animation or texture. */
    public void render(SpriteBatch batch) {
        if (!active) return;

        if (animation != null) {
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, position.x, position.y, bounds.width, bounds.height);
        } else if (texture != null) {
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        }
    }

    /** Checks collision with the player and applies damage. */
    public boolean checkPlayerHit(Player player) {
        if (!active) return false;

        if (bounds.overlaps(player.getHitbox())) {
            player.takeDamage(damage);
            active = false;
            return true;
        }
        return false;
    }

    /** Checks collision with enemies and applies damage. */
    public boolean checkEnemyHit(Array<Enemy> enemies) {
        if (!active) return false;

        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(damage);
                active = false;
                return true;
            }
        }
        return false;
    }

    /** Returns if the projectile is still active. */
    public boolean isActive() { return active; }
    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }

    /** Disposes texture to prevent memory leaks. */
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
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

    // Current position of the projectile
    private Vector2 position;
    // Velocity vector, direction and speed combined
    private Vector2 velocity;
    // Rectangle used for collision detection
    private Rectangle bounds;

    private float speed;   // Movement speed
    private float damage;  // Damage dealt on hit
    private boolean active; // Whether the projectile is active

    private Texture texture;                       // Single-frame texture
    private Animation<TextureRegion> animation;    // Animated projectile
    private float stateTime = 0f;                  // Timer for animation

    private Color color;                            // Color tint
    private float lifetime = 3f;                    // Maximum life in seconds
    private float currentLifetime = 0f;             // Time since creation

    // ------------------- Constructors -------------------

    // Constructor using color and direction
    public Projectile(float x, float y, float directionX, float directionY,
                      float speed, float damage, Color color) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);
        this.bounds = new Rectangle(x, y, 8, 8); // default size 8x8
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.color = color;
    }

    // Constructor using a texture
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

    // Constructor using an animation
    public Projectile(float x, float y, float directionX, float directionY,
                      float speed, float damage, Animation<TextureRegion> animation,
                      float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(directionX, directionY).nor().scl(speed);
        this.bounds = new Rectangle(x, y, width, height);
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        this.animation = animation;
        this.color = Color.WHITE;
    }

    // Constructor using a Vector2 direction
    public Projectile(float x, float y, Vector2 direction, float speed, float damage, Color color) {
        this(x, y, direction.x, direction.y, speed, damage, color);
    }

    // ------------------- Update Logic -------------------

    // Update projectile position and check lifetime
    public void update(float delta) {
        if (!active) return;

        // Move projectile according to velocity and delta time
        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);

        // Update animation timer
        stateTime += delta;

        // Increment lifetime and deactivate if exceeded
        currentLifetime += delta;
        if (currentLifetime >= lifetime) {
            active = false;
        }

        // Deactivate if projectile goes out of bounds
        if (position.x < -200 || position.x > 3000 || position.y < -200 || position.y > 3000) {
            active = false;
        }
    }

    // ------------------- Rendering -------------------

    public void render(SpriteBatch batch) {
        if (!active) return;

        if (animation != null) {
            // Draw animated projectile
            TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, position.x, position.y, bounds.width, bounds.height);
        } else if (texture != null) {
            // Draw single-frame projectile
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        }
        // If neither texture nor animation is assigned, nothing is drawn
    }

    // ------------------- Collision -------------------

    // Check collision with a player
    public boolean checkPlayerHit(Player player) {
        if (!active) return false;

        if (bounds.overlaps(player.getHitbox())) {
            player.takeDamage(damage); // Deal damage to player
            active = false; // Deactivate after hitting
            return true;
        }
        return false;
    }

    // Check collision with enemies
    public boolean checkEnemyHit(Array<Enemy> enemies) {
        if (!active) return false;

        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && bounds.overlaps(enemy.getBounds())) {
                enemy.takeDamage(damage); // Deal damage to enemy
                return true;
            }
        }
        return false;
    }

    // ------------------- Getters -------------------

    public boolean isActive() { return active; }
    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }

    // ------------------- Dispose -------------------

    // Dispose of texture to free memory
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
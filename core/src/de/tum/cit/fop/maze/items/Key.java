package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;

public class Key {

    // Collision bounds of the key
    private Rectangle bounds;

    // Whether the key has already been collected
    private boolean collected = false;

    // Texture used to render the key
    private Texture texture;

    // Indicates whether this is a bonus key or an exit key
    private boolean isBonus;

    // Constructor: creates a key at the given position
    public Key(float x, float y, boolean isBonus) {
        bounds = new Rectangle(x, y, 32, 32);
        texture = new Texture(Gdx.files.internal("key.png"));
        this.isBonus = isBonus;
    }

    // Checks if the player picks up the key
    public void checkPickup(Player player) {
        if (!collected && bounds.overlaps(player.getHitbox())) {

            collected = true;

            // Update player stats based on key type
            if (isBonus) {
                player.getStats().collectBonusKey();
            } else {
                player.getStats().collectExitKey();
            }

            System.out.println("Key collected! Is Bonus: " + isBonus);
        }
    }

    // Renders the key if it has not been collected
    public void render(SpriteBatch batch) {
        if (!collected) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    // Returns whether the key is collected
    public boolean isCollected() {
        return collected;
    }

    // Returns the collision bounds
    public Rectangle getBounds() {
        return bounds;
    }

    // Releases texture resources
    public void dispose() {
        texture.dispose();
    }

    // Position and size getters
    public float getX() {
        return bounds.x;
    }

    public float getY() {
        return bounds.y;
    }

    public float getWidth() {
        return bounds.width;
    }

    public float getHeight() {
        return bounds.height;
    }

    // Sets whether the key is a bonus key
    public void setBonus(boolean isBonus) {
        this.isBonus = isBonus;
    }
}
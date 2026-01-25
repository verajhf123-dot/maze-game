package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;

/**
 * Represents a collectible Key in the game.
 * Can be a bonus key or an exit key.
 */
public class Key {

    private Rectangle bounds;    // Collision box for the key
    private boolean collected = false; // Whether the key has been picked up
    private Texture texture;     // Key texture
    private boolean isBonus;     // True if this key is a bonus key

    /**
     * Constructor for the Key object.
     * @param x X position in the world
     * @param y Y position in the world
     * @param isBonus True if this key is a bonus key
     */
    public Key(float x, float y, boolean isBonus) {
        bounds = new Rectangle(x, y, 32, 32);

        // Load texture from internal files
        texture = new Texture(Gdx.files.internal("key.png"));

        this.isBonus = isBonus;

        System.out.println("Key created at (" + x + "," + y + "), isBonus=" + isBonus);
    }

    /**
     * Check if the player has picked up the key.
     * Grants the appropriate effect depending on key type.
     * @param player The player object
     */
    public void checkPickup(Player player) {
        if (!collected && bounds.overlaps(player.getHitbox())) {

            collected = true; // Mark as collected

            // Apply effect depending on key type
            if (isBonus) {
                player.getStats().collectBonusKey();
            } else {
                player.getStats().collectExitKey();
            }

            System.out.println("Key collected! Is Bonus: " + isBonus);
        }
    }

    /**
     * Render the key if it has not been collected.
     * @param batch SpriteBatch to draw on
     */
    public void render(SpriteBatch batch) {
        if (!collected) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    /**
     * Dispose the key's texture when no longer needed.
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    /** Getters and setters **/
    public boolean isCollected() { return collected; }
    public Rectangle getBounds() { return bounds; }
    public float getX() { return bounds.x; }
    public float getY() { return bounds.y; }
    public float getWidth() { return bounds.width; }
    public float getHeight() { return bounds.height; }
    public void setBonus(boolean isBonus) { this.isBonus = isBonus; }
}
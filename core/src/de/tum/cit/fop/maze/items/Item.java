package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * Abstract base class for all items in the game.
 * Items can have a texture or fallback color if the texture is missing.
 */
public abstract class Item {

    // ------------------- Fields -------------------
    protected Rectangle bounds;      // Item position and size
    protected Texture texture;       // Main texture
    protected Color fallbackColor;   // Color to use if texture is missing

    private static Texture pixel;    // Single pixel texture for fallback rendering

    // ------------------- Constructor -------------------
    /**
     * Create a new item at position (x, y) with specified size.
     * Attempts to load texture from the given path; if missing, uses fallback color.
     *
     * @param x            X position
     * @param y            Y position
     * @param size         Width and height of the item
     * @param texturePath  Path to the texture file
     * @param fallbackColor Color to use if texture is missing
     */
    protected Item(float x, float y, float size, String texturePath, Color fallbackColor) {
        this.bounds = new Rectangle(x, y, size, size);
        this.fallbackColor = fallbackColor;

        // Trim and check texture path
        if (texturePath != null) {
            texturePath = texturePath.trim();
        }

        // Load texture if file exists
        if (texturePath != null && !texturePath.isEmpty() && Gdx.files.internal(texturePath).exists()) {
            texture = new Texture(texturePath);
        }

        System.out.println("Item created: " + texturePath
                + " exists=" + (texturePath != null && Gdx.files.internal(texturePath).exists())
                + " bounds=" + bounds);
        if (texture != null) {
            System.out.println("Loaded texture size: " + texture.getWidth() + "x" + texture.getHeight());
        }
    }

    // ------------------- Render -------------------
    /**
     * Render the item using the provided SpriteBatch.
     * Uses the texture if available, otherwise draws a colored pixel rectangle.
     *
     * @param batch SpriteBatch used for drawing
     */
    public void render(SpriteBatch batch) {
        // Initialize pixel texture for fallback if not already created
        if (pixel == null) {
            if (Gdx.files.internal("pixel.png").exists()) {
                pixel = new Texture("pixel.png");
            } else {
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.WHITE);
                pixmap.fill();
                pixel = new Texture(pixmap);
                pixmap.dispose();
            }
        }

        if (texture != null) {
            // Draw actual texture
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            // Draw colored rectangle if texture missing
            batch.setColor(fallbackColor);
            batch.draw(pixel, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE); // Reset color to default
        }
    }

    // ------------------- Getters -------------------
    public Rectangle getBounds() {
        return bounds;
    }

    public float getX() {
        return bounds.x;
    }

    public float getY() {
        return bounds.y;
    }

    // ------------------- Abstract Method -------------------
    /**
     * Called when an entity (e.g., player) picks up this item.
     * Implement specific pickup behavior in subclasses.
     *
     * @param entity The entity that picked up the item
     */
    public abstract void onPickup(Object entity);
}
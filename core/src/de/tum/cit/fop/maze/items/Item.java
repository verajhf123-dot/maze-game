package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

public abstract class Item {

    protected Rectangle bounds; // Item's position and size
    protected Texture texture; // Texture of the item
    protected Color fallbackColor; // Color to use if texture is missing

    private static Texture pixel; // Single pixel texture used as fallback

    // Constructor to create an item
    protected Item(float x, float y, float size, String texturePath, Color fallbackColor) {
        this.bounds = new Rectangle(x, y, size, size); // Set position and size
        this.fallbackColor = fallbackColor;

        if (texturePath != null) {
            texturePath = texturePath.trim(); // Remove whitespace
        }

        // Load texture if path is valid and exists
        if (texturePath != null && !texturePath.isEmpty()
                && Gdx.files.internal(texturePath).exists()) {
            texture = new Texture(texturePath);
        }

        // Debug prints
        System.out.println("Item created: " + texturePath + " exists="
                + (texturePath != null && Gdx.files.internal(texturePath).exists())
                + " bounds=" + bounds);
        System.out.println("Create Item: " + getClass().getSimpleName()
                + ", texturePath=" + texturePath);
        if (texture != null) {
            System.out.println("Loaded texture size: " + texture.getWidth() + "x" + texture.getHeight());
        }
    }

    // Render the item on screen
    public void render(SpriteBatch batch) {
        System.out.println("Rendering item at " + bounds.x + "," + bounds.y);

        // Create pixel texture if needed
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

        // Draw actual texture if available, otherwise draw fallback color
        if (texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.setColor(fallbackColor);
            batch.draw(pixel, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE); // Reset batch color
        }
    }

    // Get the bounding rectangle for collision
    public Rectangle getBounds() {
        return bounds;
    }

    // Abstract method: define what happens when the item is picked up
    public abstract void onPickup(Object entity);

    // Get X position
    public float getX() {
        return bounds.x;
    }

    // Get Y position
    public float getY() {
        return bounds.y;
    }
}
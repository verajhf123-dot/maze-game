package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

public abstract class Item {

    protected Rectangle bounds;
    protected Texture texture;
    protected Color fallbackColor;

    private static Texture pixel;

    protected Item(float x, float y, float size, String texturePath, Color fallbackColor) {
        this.bounds = new Rectangle(x, y, size, size);
        this.fallbackColor = fallbackColor;

        if (texturePath != null) {
            texturePath = texturePath.trim();
        }

        if (texturePath != null && !texturePath.isEmpty()
                && Gdx.files.internal(texturePath).exists()) {
            texture = new Texture(texturePath);
        }


        if (texturePath != null && Gdx.files.internal(texturePath).exists()) {
            texture = new Texture(texturePath);
        }
        System.out.println("Item created: " + texturePath + " exists="
                + (texturePath != null && Gdx.files.internal(texturePath).exists())
                + " bounds=" + bounds);
        System.out.println("Create Item: " + getClass().getSimpleName()
                + ", texturePath=" + texturePath);
        if (texture != null) {
            System.out.println("Loaded texture size: " + texture.getWidth() + "x" + texture.getHeight());
        }
    }

    public void render(SpriteBatch batch) {
        System.out.println("Rendering item at " + bounds.x + "," + bounds.y);


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
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            batch.setColor(fallbackColor);
            batch.draw(pixel, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
        }

    }

    public Rectangle getBounds() {
        return bounds;
    }

    public abstract void onPickup(Object entity);

    public float getX() {
        return bounds.x;
    }

    public float getY() {
        return bounds.y;
    }
}

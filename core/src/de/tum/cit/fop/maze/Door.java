package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Represents a locked door in the game world.
 * The door blocks the player until a key is used to open it.
 * It handles collision checks, open state, and rendering when closed.
 */

public class Door {

    private Rectangle bounds;
    private boolean open = false;
    private Texture doorTexture;
    private TextureRegion closedDoorRegion;
    /**
     * Creates a door with collision bounds and a closed-door texture.
     * It tries to load the door image file; if it is missing, a fallback texture is used instead.
     */


    public Door(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);


        String texturePath = "gate.png";

        if (Gdx.files.internal(texturePath).exists()) {

            doorTexture = new Texture(Gdx.files.internal(texturePath));

            closedDoorRegion = new TextureRegion(doorTexture);


        } else {
            System.err.println("Warning: things.png missing. Using Blue Box fallback.");
            createFallbackTexture();
        }
    }

    /**
     * Creates a simple fallback texture(blue box)if the door texture is missing.
     */
    private void createFallbackTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLUE);
        pixmap.fill();
        Texture temp = new Texture(pixmap);
        closedDoorRegion = new TextureRegion(temp);
        pixmap.dispose();
    }

    /**
     * check the logic whether the door can be opened.
     * The door opens only if the player overlaps the door and has a key.
     * @param player the player trying to open the door.
     */
    public void tryOpen(Player player) {
        if (!open && bounds.overlaps(player.getHitbox())
                 && player.getStats().hasKey()) {
            player.getStats().useKey();
            open = true;
            System.out.println("Door opened!");
        }
    }

    /**
     * Renders the closwd door(only when the door is not open)
     * @param batch SpriteBach used to draw the door texture region.
     */
    public void render(SpriteBatch batch) {
        if (!open && closedDoorRegion != null) {
            float drawWidth = 48f;
            float drawHeight = 48f;
            float drawX = bounds.x + (bounds.width - drawWidth) / 2;
            float drawY = bounds.y + (bounds.height - drawHeight) / 2;
            batch.draw(closedDoorRegion,
                    drawX,
                    drawY,
                    drawWidth,
                    drawHeight);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * dispose texture to free GPU resources.
     */
    public void dispose() {
        if (doorTexture != null) {
            doorTexture.dispose();
        }
    }
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
}

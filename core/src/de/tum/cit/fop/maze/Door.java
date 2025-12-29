package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Door {

    private Rectangle bounds;
    private boolean open = false;

    private Texture closedTexture;
    private Texture openTexture;

    public Door(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);
        closedTexture = new Texture(Gdx.files.internal("items/door_closed.png"));
        openTexture = new Texture(Gdx.files.internal("items/door_open.png"));
    }

    public void tryOpen(Player player) {
        if (!open && bounds.overlaps(player.getHitbox())
                 && player.getStats().hasKey()) {
            player.getStats().useKey();
            open = true;
            System.out.println("Door opened!");
        }
    }

    public void render(SpriteBatch batch) {
        Texture tex = open ? openTexture : closedTexture;
        batch.draw(tex, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public boolean isOpen() {
        return open;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        closedTexture.dispose();
        openTexture.dispose();
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
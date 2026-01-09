package de.tum.cit.fop.maze.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;

public class Key {

    private Rectangle bounds;
    private boolean collected = false;
    private Texture texture;
    private boolean isBonus;

    public Key(float x, float y,boolean isBonus) {
        bounds = new Rectangle(x, y, 32, 32);
        texture = new Texture(Gdx.files.internal("key.png"));
        this.isBonus = isBonus;
    }

    public void checkPickup(Player player) {
        if (!collected && bounds.overlaps(player.getHitbox())) {

            collected = true;


            if (isBonus) {
                player.getStats().collectBonusKey();
            } else {
                player.getStats().collectExitKey();
            }

            System.out.println("Key collected! Is Bonus: " + isBonus);
        }


    }

    public void render(SpriteBatch batch) {
        if (!collected) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
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

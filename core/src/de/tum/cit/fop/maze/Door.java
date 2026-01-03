package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import static com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.TextureRegion;

public class Door {

    private Rectangle bounds;
    private boolean open = false;

    private Texture spriteSheet;
    // 修改2: 我们需要一个 TextureRegion 来存放“切”出来的门
    private TextureRegion closedDoorRegion;

    public Door(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);


        String texturePath = "things.png";

        if (Gdx.files.internal(texturePath).exists()) {

            spriteSheet = new Texture(Gdx.files.internal(texturePath));

            closedDoorRegion = new TextureRegion(spriteSheet, 0, 0, 32, 32);
        } else {
            System.err.println("Warning: things.png missing. Using Blue Box fallback.");
            createFallbackTexture();
        }
    }
    private void createFallbackTexture() {
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLUE);
        pixmap.fill();
        Texture temp = new Texture(pixmap);
        closedDoorRegion = new TextureRegion(temp);
        pixmap.dispose();
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
        if (!open && closedDoorRegion != null) {
            batch.draw(closedDoorRegion, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
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
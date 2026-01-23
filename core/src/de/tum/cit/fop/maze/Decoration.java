package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
/**
 * Represents a non-interactive decorative object in the level.
 * Decorations are visual-only and do not affect gameplay or collision.
 */

public class Decoration {
    /** World x-position (in pixels). */
    public final float x;
    public final float y;
    public final Texture tex;
    /** Width of the decoration (in pixels). */
    public final float w;
    public final float h;
    /**
     * Creates a decoration with position, texture, and size.
     *
     * @param x x-position in world coordinates
     * @param y y-position in world coordinates
     * @param tex texture to render
     * @param w width of the decoration
     * @param h height of the decoration
     */

    public Decoration(float x, float y, Texture tex, float w, float h) {
        this.x = x;
        this.y = y;
        this.tex = tex;
        this.w = w;
        this.h = h;
    }
    /**
     * Renders the decoration centered on a tile.
     *
     * @param batch SpriteBatch used for drawing
     */
    public void render(SpriteBatch batch) {
        float offsetX = (w - Wall.TILE_SIZE) / 2f;
        batch.draw(tex, x - offsetX, y, w, h);
    }
}

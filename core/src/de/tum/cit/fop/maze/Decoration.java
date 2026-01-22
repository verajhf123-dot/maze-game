package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Decoration {
    public final float x;
    public final float y;
    public final Texture tex;
    public final float w;
    public final float h;

    public Decoration(float x, float y, Texture tex, float w, float h) {
        this.x = x;
        this.y = y;
        this.tex = tex;
        this.w = w;
        this.h = h;
    }

    public void render(SpriteBatch batch) {
        float offsetX = (w - Wall.TILE_SIZE) / 2f;
        batch.draw(tex, x - offsetX, y, w, h);
    }
}

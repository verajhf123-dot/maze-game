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

    // 不要 static final 直接 new
    private static Texture pixel;

    protected Item(float x, float y, float size, String texturePath, Color fallbackColor) {
        this.bounds = new Rectangle(x, y, size, size);
        this.fallbackColor = fallbackColor;

        // 加载物品图片（可选）
        if (texturePath != null && Gdx.files.internal(texturePath).exists()) {
            texture = new Texture(texturePath);
        }
    }

    public void render(SpriteBatch batch) {

        // ⭐ 这里修改为安全加载 pixel
        if (pixel == null) {
            if (Gdx.files.internal("pixel.png").exists()) {
                pixel = new Texture("pixel.png");
            } else {
                // 文件不存在就生成 1x1 白色纹理
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
}

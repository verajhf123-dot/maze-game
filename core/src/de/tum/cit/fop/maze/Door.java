package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;


public class Door {

    private Rectangle bounds;
    private boolean open = false;

    private Texture doorTexture;
    // 修改2: 我们需要一个 TextureRegion 来存放“切”出来的门
    private TextureRegion closedDoorRegion;

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
            // 1. 设置你想要的显示大小
            // 既然32太小，64太大，我们取个中间值 48 试试（或者你可以改成 40）
            float drawWidth = 48f;
            float drawHeight = 48f;

            // 2. 关键步骤：计算“偏移量”让图片居中
            // 公式原理：(碰撞箱宽度 - 图片宽度) / 2 = 需要移动的距离
            float drawX = bounds.x + (bounds.width - drawWidth) / 2;
            float drawY = bounds.y + (bounds.height - drawHeight) / 2;

            // 3. 画图（使用计算好的新坐标 drawX, drawY）
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

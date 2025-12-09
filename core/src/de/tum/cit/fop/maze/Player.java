package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    // 给一个默认位置，免得空指针
    private Vector2 position = new Vector2(0, 0);
    private Rectangle bounds = new Rectangle(0, 0, 0, 0);

    public Player(float x, float y) {
        // 假装初始化了
    }

    public void update(float delta) {
        // 假装在更新，实际什么都不做
    }

    public void render(SpriteBatch batch) {
        // 假装在画图，实际什么都不画（隐形人）
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}

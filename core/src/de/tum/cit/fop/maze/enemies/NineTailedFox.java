package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class NineTailedFox extends Enemy {
    public NineTailedFox(float x, float y) {
        super(x, y, 32, 32); // 32x32 像素大小
    }

    @Override
    public void update(float delta) {
        // TODO: AI逻辑
        if (targetPosition != null) {
            // 简单的朝目标移动
            position.x += (targetPosition.x - position.x) * 0.01f;
            position.y += (targetPosition.y - position.y) * 0.01f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // TODO: 使用mobs.png中的纹理
        // 暂时用空白，后续添加纹理
    }
}

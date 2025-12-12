package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class QiongQi extends Enemy {

    public QiongQi(float x, float y) {
        super(x, y, 48, 48); // 穷奇体型稍大，48x48像素
    }

    @Override
    public void update(float delta) {
        // TODO: 穷奇特有的AI逻辑
        if (targetPosition != null) {
            // 穷奇移动更快，追击更积极
            float speed = 0.02f; // 移动速度系数

            // 计算朝向目标的方向
            float dx = targetPosition.x - position.x;
            float dy = targetPosition.y - position.y;

            // 移动
            position.x += dx * speed;
            position.y += dy * speed;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        // TODO: 使用mobs.png中穷奇的纹理
        // 暂时用空白，后续添加纹理
    }

    // 穷奇的特殊能力方法（可以根据需要添加）
    public void activateRageMode() {
        // 狂暴模式，增加攻击力和速度
    }

    public void useSpecialAttack() {
        // 特殊攻击技能
    }
}
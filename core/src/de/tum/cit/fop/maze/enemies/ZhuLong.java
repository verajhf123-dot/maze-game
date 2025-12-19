package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

public class ZhuLong extends Enemy {
    private boolean eyesOpen = true; // 睁眼状态
    private float dayNightTimer = 0f;

    protected float health;
    protected float maxHealth;
    protected float speed;
    protected boolean isAlive = true;

    public ZhuLong(float x, float y) {
        super(x, y, 32, 32);
        this.health = 150;
        this.maxHealth = 150;
        this.speed = 40f; // 比普通敌人慢
    }

    @Override
    public void update(float delta) {
        if (!isAlive) return;

        // 更新昼夜计时器
        updateDayNightCycle(delta);

        // 根据睁眼状态调整行为
        if (eyesOpen) {
            // 睁眼：正常移动和攻击
            if (targetPosition != null) {
                position.x += (targetPosition.x - position.x) * 0.3f * delta;
                position.y += (targetPosition.y - position.y) * 0.3f * delta;
            }
        } else {
            // 闭眼：移动变慢或停止
            speed = 20f;
        }

    }

    private void updateDayNightCycle(float delta) {
        dayNightTimer += delta;

        // 每5秒切换一次睁眼/闭眼状态
        if (dayNightTimer >= 5.0f) {
            eyesOpen = !eyesOpen;
            dayNightTimer = 0f;
            System.out.println("ZhuLong eyes: " + (eyesOpen ? "OPEN" : "CLOSED"));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        // 使用shapeRenderer绘制（临时方案）
        // 睁眼：黄色，闭眼：深黄色
        com.badlogic.gdx.graphics.glutils.ShapeRenderer sr = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        sr.setProjectionMatrix(batch.getProjectionMatrix());
        sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        sr.setColor(eyesOpen ? Color.YELLOW : Color.ORANGE);
        sr.rect(position.x, position.y, 32, 32);

        // 绘制眼睛状态指示器
        sr.setColor(eyesOpen ? Color.WHITE : Color.DARK_GRAY);
        sr.circle(position.x + 10, position.y + 22, 4);
        sr.circle(position.x + 22, position.y + 22, 4);

        sr.end();
    }

    // 特殊方法：获取当前状态
    public boolean isEyesOpen() {
        return eyesOpen;
    }
}
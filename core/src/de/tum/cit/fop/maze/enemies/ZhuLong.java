package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class ZhuLong extends Enemy {
    private boolean eyesOpen = true; // 睁眼状态
    private float dayNightTimer = 0f;
    private ShapeRenderer shapeRenderer;
    private Texture texture; // 可选的纹理
    private Vector2 targetPosition; // 添加目标位置变量

    public ZhuLong(float x, float y) {
        super(x, y, 32, 32);

        // 初始化 Enemy 基类的属性
        this.health = 150f;
        this.maxHealth = 150f;
        this.speed = 40f;
        this.attackDamage = 25f;
        this.attackRange = 60f;
        this.detectionRange = 180f;

        this.shapeRenderer = new ShapeRenderer();

        // 可选：加载纹理
        try {
            texture = safeLoadTexture("enemies/zhulong.png");
        } catch (Exception e) {
            // 如果纹理不存在，使用形状渲染器
            texture = null;
        }
    }

    @Override
    public void update(float delta) {
        if (!isAlive()) return;

        // 更新昼夜计时器
        updateDayNightCycle(delta);

        // 根据睁眼状态调整速度
        float currentSpeed = eyesOpen ? speed : speed * 0.5f;

        // 如果设置了目标位置，向目标移动
        if (targetPosition != null) {
            Vector2 direction = new Vector2(
                    targetPosition.x - position.x,
                    targetPosition.y - position.y
            );

            float distance = direction.len();
            if (distance > 1f) {
                direction.nor();
                velocity.set(direction.x * currentSpeed, direction.y * currentSpeed);
            } else {
                velocity.set(0, 0);
            }
        }

        // 调用父类的更新逻辑
        super.update(delta);
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
        if (!isAlive()) return;

        // 如果有纹理，使用纹理渲染
        if (texture != null) {
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        }
    }

    // ========== 实现抽象方法 ==========

    @Override
    public void attack() {
        // 烛龙的攻击逻辑
        // 睁眼时攻击力更强
        float damageMultiplier = eyesOpen ? 1.5f : 0.8f;
        float actualDamage = attackDamage * damageMultiplier;

        System.out.println("ZhuLong attacks! Damage: " + actualDamage +
                " (Eyes: " + (eyesOpen ? "OPEN" : "CLOSED") + ")");
    }

    @Override
    protected void onDeath() {
        System.out.println("ZhuLong has been defeated!");
        // 可以在这里添加死亡效果、掉落物品等
    }

    // ========== 新增方法 ==========

    public void setTargetPosition(Vector2 target) {
        this.targetPosition = target;
    }

    public Vector2 getTargetPosition() {
        return targetPosition;
    }

    // 特殊方法：获取当前状态
    public boolean isEyesOpen() {
        return eyesOpen;
    }

    public float getDayNightTimer() {
        return dayNightTimer;
    }

    public void setEyesOpen(boolean open) {
        this.eyesOpen = open;
        dayNightTimer = 0f; // 重置计时器
    }
    public Color getFallbackBodyColor(){return eyesOpen ? Color.YELLOW : Color.ORANGE;}
    public boolean hasTexture() {return texture != null;}
    // 清理资源
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (texture != null) {
            texture.dispose();
        }
    }
}
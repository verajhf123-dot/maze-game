package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
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
    private Texture[] formTextures; // 改为纹理数组
    private Vector2 targetPosition;
    private static Texture fallbackTexture;

    // 新添加的变量用于形态切换
    private int currentForm = 0; // 当前形态 (0-3)
    private float formTimer = 0f; // 形态计时器
    private float formSwitchInterval = 2.0f; // 每2秒切换一次

    public ZhuLong(float x, float y) {
        super(x, y, 16, 16);

        // 初始化 Enemy 基类的属性
        this.health = 150f;
        this.maxHealth = 300f;
        this.speed = 40f;
        this.attackDamage = 15f;
        this.attackRange = 60f;
        this.detectionRange = 180f;

        formTextures = new Texture[4];
        loadFormTextures();

        if (fallbackTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            fallbackTexture = new Texture(p);
            p.dispose();
        }
    }

    private void loadFormTextures() {
        String[] texturePaths = {
                "enemies/zhulong1.png",
                "enemies/zhulong2.png",
                "enemies/zhulong3.png",
                "enemies/zhulong4.png"
        };

        for (int i = 0; i < 4; i++) {
            try {
                formTextures[i] = new Texture(Gdx.files.internal(texturePaths[i]));
                System.out.println("Loaded ZhuLong form " + (i+1) + ": " + texturePaths[i]);
            } catch (Exception e) {
                System.out.println("Failed to load ZhuLong form " + (i+1) + ", trying fallback...");
                // 如果某个形态图片缺失，尝试使用zhulong.png作为备用
                try {
                    if (i == 0) { // 如果是第一个形态，加载默认纹理
                        Texture defaultTex = new Texture(Gdx.files.internal("enemies/zhulong.png"));
                        for (int j = 0; j < 4; j++) {
                            formTextures[j] = defaultTex;
                        }
                        System.out.println("Using default zhulong.png for all forms");
                        break;
                    }
                } catch (Exception e2) {
                    formTextures[i] = null;
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        if (!isAlive()) return;

        // 更新昼夜计时器
        updateDayNightCycle(delta);

        // 新增：更新形态切换计时器
        formTimer += delta;
        if (formTimer >= formSwitchInterval) {
            currentForm = (currentForm + 1) % 4; // 0->1->2->3->0
            formTimer = 0f;
            System.out.println("ZhuLong switched to form " + (currentForm + 1));
        }

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

        // 修改：使用当前形态的纹理
        Texture currentTexture = formTextures[currentForm];

        if (currentTexture != null) {
            // 如果纹理存在，直接绘制当前形态的图片
            batch.draw(currentTexture, position.x, position.y, bounds.width, bounds.height);
        } else {
            // 如果纹理缺失，使用原代码的备用颜色方块
            // 但这里我们加上形态指示：用不同颜色区分不同形态
            Color[] formColors = {
                    Color.RED,    // 形态1
                    Color.BLUE,   // 形态2
                    Color.GREEN,  // 形态3
                    Color.YELLOW  // 形态4
            };

            // 闭眼时颜色变暗（原代码逻辑）
            Color displayColor = eyesOpen ? formColors[currentForm] : Color.DARK_GRAY;

            batch.setColor(displayColor);
            batch.draw(fallbackTexture, position.x, position.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
        }
    }

    // ========== 实现抽象方法 ==========

    @Override
    public void attack() {
        // 烛龙的攻击逻辑（原代码保持不变，只是加上形态信息）
        // 睁眼时攻击力更强
        float damageMultiplier = eyesOpen ? 1.5f : 0.8f;
        float actualDamage = attackDamage * damageMultiplier;

        System.out.println("ZhuLong attacks! Form: " + (currentForm + 1) +
                ", Damage: " + actualDamage +
                " (Eyes: " + (eyesOpen ? "OPEN" : "CLOSED") + ")");
    }

    @Override
    public void adjustDifficulty(int level) {
        this.maxHealth = 200 + (level * 40);
        this.health = this.maxHealth;
        this.attackDamage = 10 + (level * 3);
        this.speed = 40f + (level * 2f);
    }

    @Override
    protected void onDeath() {
        System.out.println("ZhuLong has been defeated!");
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

    public int getCurrentForm() {
        return currentForm;
    }

    public float getFormTimer() {
        return formTimer;
    }

    public void setCurrentForm(int form) {
        if (form >= 0 && form < 4) {
            this.currentForm = form;
            this.formTimer = 0f;
        }
    }

    // 清理资源（修改后需要清理四个纹理）
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        // 清理四个形态的纹理
        for (int i = 0; i < 4; i++) {
            if (formTextures[i] != null) {
                // 检查是否是共享纹理（比如默认纹理被多个形态共享）
                boolean isShared = false;
                for (int j = 0; j < i; j++) {
                    if (formTextures[j] == formTextures[i]) {
                        isShared = true;
                        break;
                    }
                }
                if (!isShared) {
                    formTextures[i].dispose();
                }
                formTextures[i] = null;
            }
        }
    }
}
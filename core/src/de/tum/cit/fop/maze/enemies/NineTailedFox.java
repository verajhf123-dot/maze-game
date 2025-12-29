package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;

public class NineTailedFox extends Enemy {
    private Texture texture;
    private float specialAttackCooldown = 8f;
    private float currentSpecialCooldown = 0;
    private boolean isUsingSpecial = false;
    private static Texture fallbackTexture;


    public NineTailedFox(float x, float y) {
        super(x, y, 16, 16);
        this.speed = 120f;
        this.maxHealth = 150f;
        this.health = maxHealth;
        this.attackDamage = 25f;
        this.attackRange = 200f;
        this.detectionRange = 300f;

        try {
            this.texture = new Texture(Gdx.files.internal("enemies/nine_tailed_fox.png"));
        } catch (Exception e) {
            System.out.println("NineTailedFox texture missing, using red box.");
            this.texture = null;
        }

        // 创建白块
        if (fallbackTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            fallbackTexture = new Texture(p);
            p.dispose();
        }

        this.texture = safeLoadTexture("enemies/nine_tailed_fox.png");
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (currentSpecialCooldown > 0) {
            currentSpecialCooldown -= delta;
        }

        if (isUsingSpecial) {
            performSpecialAttack();
        }
    }

    @Override
    public void render(SpriteBatch batch) {

        if (texture != null) {
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        } else {
            batch.setColor(Color.RED);
            batch.draw(fallbackTexture, position.x, position.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
        }
        if (texture != null) {
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        }
        else {
            // fallback 方块
            batch.end();
        }

    }

    @Override
    public void attack() {
        // 普通攻击：发射一个火球
        if (!isUsingSpecial) {
            shootFireball();
        }
    }

    private void shootFireball() {
        // 创建火弹攻击
        Vector2 direction = new Vector2(1, 0); // 默认方向，实际应该朝向玩家
        // 这里需要创建Projectile对象
    }

    private void performSpecialAttack() {
        // 九尾狐特殊攻击：向多个方向发射火球
        for (int i = 0; i < 9; i++) {
            float angle = i * 40 * MathUtils.degreesToRadians;
            Vector2 direction = new Vector2(MathUtils.cos(angle), MathUtils.sin(angle));
            // 发射火球
        }
    }

    public void activateSpecialAttack() {
        if (currentSpecialCooldown <= 0) {
            isUsingSpecial = true;
            currentSpecialCooldown = specialAttackCooldown;
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    isUsingSpecial = false;
                }
            }, 3.0f);
        }
    }

    @Override
    protected void onDeath() {
        // 死亡效果
        // 掉落物品、播放动画等
    }
    @Override
    public Color getFallbackBodyColor(){
        return Color.PURPLE;
    }
}

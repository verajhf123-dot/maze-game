package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;


public class QiongQi extends Enemy {
    private Texture texture;
    private static Texture fallbackTexture;

    private boolean isCharging = false;
    private float chargeSpeed = 300f;
    private float normalSpeed;
    private float chargeCooldown = 6f;
    private float currentChargeCooldown = 0;
    private Vector2 chargeDirection;

    public QiongQi(float x, float y) {
        super(x, y, 16, 16);
        this.normalSpeed = 80f;
        this.speed = normalSpeed;
        this.maxHealth = 300f;
        this.health = maxHealth;
        this.attackDamage = 40f;
        this.attackRange = 50f;
        this.detectionRange = 250f;

        try {
            this.texture = new Texture(Gdx.files.internal("enemies/qiongqi.png"));
        } catch (Exception e) {
            System.out.println("QiongQi texture missing. Using Blue Box.");
        }

        if (fallbackTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            fallbackTexture = new Texture(p);
            p.dispose();
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (currentChargeCooldown > 0) {
            currentChargeCooldown -= delta;
        }

        if (isCharging) {
            performCharge(delta);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, position.x, position.y, bounds.width, bounds.height);
        } else {
            batch.setColor(isCharging ? Color.CYAN : Color.BLUE);
            batch.draw(fallbackTexture, position.x, position.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void attack() {
        // 穷奇普通攻击：近战撕咬
        if (!isCharging) {
            performMeleeAttack();
        }
    }

    private void performMeleeAttack() {
        // 近战攻击逻辑
        // 检查范围内是否有玩家
    }

    public void startCharge(Vector2 direction) {
        if (currentChargeCooldown <= 0 && !isCharging) {
            isCharging = true;
            chargeDirection = direction.nor();
            speed = chargeSpeed;
            currentChargeCooldown = chargeCooldown;
        }
    }

    private void performCharge(float delta) {
        // 沿冲锋方向移动
        velocity.set(chargeDirection.x * speed, chargeDirection.y * speed);

        // 检查冲锋是否结束（计时或碰撞）
        // 这里需要添加碰撞检测逻辑
    }

    public void endCharge() {
        isCharging = false;
        speed = normalSpeed;
        velocity.set(0, 0);
    }

    @Override
    protected void onDeath() {
        // 死亡效果
    }
}
package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;



public class Player implements CollidableEntity {

    private Texture texture;
    private TextureRegion currentFrame;

    private TextureRegion downFrame;
    private TextureRegion upFrame;
    private TextureRegion leftFrame;
    private TextureRegion rightFrame;


    private Vector2 position;
    private Vector2 velocity;

    private float speed = 120f;
    private float runMultiplier = 1.6f;

    private boolean isHurt = false;
    private float hurtTimer = 0;

    private Rectangle hitbox;

    private PlayerStats stats;
    private float damageCooldownTimer = 0f;
    private static final float DAMAGE_COOLDOWN = 0.5f; // 0.5秒内只吃一次伤害
    private float damageColorTimer = 0f;




    public Player(float x, float y) {
        this.texture = new Texture("character.png");
        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 32);

        if (tmp.length >= 4) {
            downFrame = tmp[0][0];
            rightFrame = tmp[1][0];
            upFrame = tmp[2][0];
            leftFrame = tmp[3][0];
        } else {
            // 保底防止报错
            downFrame = new TextureRegion(texture, 0, 0, 16, 32);
            rightFrame = downFrame;
            upFrame = downFrame;
            leftFrame = downFrame;
        }

        // 默认初始面朝下
        this.currentFrame = downFrame;

        // ... 后面的代码保持不变 ...
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.stats = new PlayerStats();
        this.hitbox = new Rectangle(x, y, 14, 14);
    }



    public void update(float delta, boolean up, boolean down, boolean left, boolean right, boolean run) {
        if (damageColorTimer > 0) {
            damageColorTimer -= delta;
        }
        damageCooldownTimer = Math.max(0f, damageCooldownTimer - delta);


        float currentSpeed = speed * (run ? runMultiplier : 1f);

        // 1. 重置速度
        velocity.set(0, 0);

        // 2. 根据按键设置速度和朝向
        if (up) {
            velocity.y = currentSpeed;
            currentFrame = upFrame;
        }
        if (down) {
            velocity.y = -currentSpeed;
            currentFrame = downFrame;
        }
        if (left) {
            velocity.x = -currentSpeed;
            currentFrame = leftFrame;
        }
        if (right) {
            velocity.x = currentSpeed;
            currentFrame = rightFrame;
        }
        damageCooldownTimer = Math.max(0f, damageCooldownTimer - delta);



        if (isHurt) {
            hurtTimer -= delta;
            if (hurtTimer <= 0) {
                isHurt = false;
            }
        }


    }

    public void triggerDamageVFX() {
        this.damageColorTimer = 1.0f; // 设置特效持续时间为1秒
    }


    public void render(SpriteBatch batch) {
        if (currentFrame == null) return;

        if (damageColorTimer > 0) {
            batch.setColor(Color.RED);
        } else {
            batch.setColor(Color.WHITE); // 确保非受伤状态是正常的 [cite: 27]
        }



        float drawWidth = 32f;
        float drawHeight = 64f;

        // 用真实 hitbox 尺寸，不要写死 24
        float hitW = hitbox.width;
        float hitH = hitbox.height;

        float offsetX = (drawWidth - hitW) / 2f;


        float drawX = position.x - (drawWidth - hitbox.width) / 2f;


        float drawY = position.y;


        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);


        batch.setColor(Color.WHITE);



    }


    public void takeDamage(int dmg) {
        takeDamage((float) dmg);
    }



    // 添加 float 版本（重载方法）


    public void takeDamage(float dmg) {
        if (getHealth() <= 0) return;

        if (damageCooldownTimer > 0f) return;

        int damageInt = Math.round(dmg);
        stats.takeDamage(damageInt);

        isHurt = true;
        hurtTimer = 0.25f;

        damageCooldownTimer = DAMAGE_COOLDOWN;
    }



    // 添加 getHealth 和 getMaxHealth 方法（GameScreen 需要这些）
    public float getHealth() {
        return stats.getHealth();
    }

    public float getMaxHealth() {
        return stats.getMaxHealth();
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void syncPositionToHitbox() {
        this.position.set(hitbox.x, hitbox.y);
    }


    public Rectangle getHitbox() {
        return hitbox;
    }

    public PlayerStats getStats() {
        return stats;
    }


    public com.badlogic.gdx.math.Vector2 getPosition() {
        return position;
    }

    public void dispose() {
        texture.dispose();
    }
}




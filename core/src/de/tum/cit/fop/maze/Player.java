package de.tum.cit.fop.maze;

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
        this.hitbox = new Rectangle(x, y, 24, 24);
    }

    public void update(float dt, boolean up, boolean down, boolean left, boolean right, boolean run) {
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

        if (isHurt) {
            hurtTimer -= dt;
            if (hurtTimer <= 0) {
                isHurt = false;
            }
        }
    }


    public void render(SpriteBatch batch) {
        if (currentFrame != null) {
           float drawWidth = 32f;
           float drawHeight = 64f;
           float hitboxWidth = 24f;
           float hitboxHeight = 24f;

           float offsetX = (drawWidth - hitboxWidth) / 2f;
           float drawX =position.x - offsetX;
           float drawY = position.y;
            batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
        }
    }

    public void takeDamage(int dmg) {
        stats.takeDamage(dmg);
        isHurt = true;
        hurtTimer = 0.25f;
    }

    // 添加 float 版本（重载方法）
    public void takeDamage(float dmg) {
        // 将 float 转换为 int（四舍五入）
        int damageInt = Math.round(dmg);
        stats.takeDamage(damageInt);
        isHurt = true;
        hurtTimer = 0.25f;
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




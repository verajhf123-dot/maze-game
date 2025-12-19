package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;



public class Player implements CollidableEntity{

    private Texture texture;
    private TextureRegion currentFrame;


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
        this.currentFrame = new TextureRegion(texture,0,0,16,32);//等待后面我们有人物图像再去显示，这个暂时不用

        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0,0);
        this.stats = new PlayerStats();
        this.hitbox = new Rectangle(x, y, 64, 128);
    }

    public void update(float dt, boolean up, boolean down, boolean left, boolean right, boolean run) {

        float currentSpeed = speed * (run ? runMultiplier : 1f);

        velocity.set(0, 0);

        if (up)    velocity.y = currentSpeed;
        if (down)  velocity.y = -currentSpeed;
        if (left)  velocity.x = -currentSpeed;
        if (right) velocity.x = currentSpeed;

        // ✅ 注意：这里不要 position.add(...)
        // ✅ 注意：这里不要 hitbox.setPosition(...)

        if (isHurt) {
            hurtTimer -= dt;
            if (hurtTimer <= 0) {
                isHurt = false;
            }
        }
    }



    public void render(SpriteBatch batch) {
        if(currentFrame != null) {
            batch.draw(currentFrame, position.x, position.y,64,128);
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

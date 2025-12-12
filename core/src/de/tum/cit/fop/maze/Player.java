package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {

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
        this.hitbox = new Rectangle(x, y, 32, 32);
    }

    public void update(float dt, boolean up, boolean down, boolean left, boolean right, boolean run) {

        float currentSpeed = speed * (run ? runMultiplier : 1);

        velocity.set(0,0);
        if(up) velocity.y = currentSpeed;
        if(down) velocity.y = -currentSpeed;
        if(left) velocity.x = -currentSpeed;
        if(right) velocity.x = currentSpeed;

        position.add(velocity.x * dt, velocity.y * dt);

        hitbox.setPosition(position.x, position.y);

        if(isHurt) {
            hurtTimer -= dt;
            if(hurtTimer <= 0) {
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

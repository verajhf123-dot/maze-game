package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

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

    // 速度增益相关变量
    private float speedBuffMultiplier = 1.0f;
    private float speedBuffTimer = 0.0f;
    private float originalSpeed = 120f;

    private boolean isHurt = false;
    private float hurtTimer = 0;

    private Rectangle hitbox;

    private PlayerStats stats;
    private float damageCooldownTimer = 0f;
    private static final float DAMAGE_COOLDOWN = 0.5f;
    private float damageColorTimer = 0f;

    private boolean isInvincible = false;
    private float invincibleTimer = 0f;
    private static final float INVINCIBLE_DURATION = 3.0f;
    private float blinkTimer = 0f;
    private static final float BLINK_SPEED = 10f;

    public Player(float x, float y) {
        this.texture = new Texture("character.png");
        TextureRegion[][] tmp = TextureRegion.split(texture, 16, 32);

        if (tmp.length >= 4) {
            downFrame = tmp[0][0];
            rightFrame = tmp[1][0];
            upFrame = tmp[2][0];
            leftFrame = tmp[3][0];
        } else {
            downFrame = new TextureRegion(texture, 0, 0, 16, 32);
            rightFrame = downFrame;
            upFrame = downFrame;
            leftFrame = downFrame;
        }

        this.currentFrame = downFrame;

        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.stats = new PlayerStats();
        this.hitbox = new Rectangle(x, y, 14, 14);
        this.originalSpeed = speed;
    }

    public void update(float delta, boolean up, boolean down, boolean left, boolean right, boolean run, List<Wall> walls) {
        if (isInvincible) {
            invincibleTimer -= delta;
            blinkTimer += delta * BLINK_SPEED;
            if (invincibleTimer <= 0) {
                isInvincible = false;
                System.out.println("Invincibility ended");
            }
        }

        if (speedBuffTimer > 0) {
            speedBuffTimer -= delta;
            if (speedBuffTimer <= 0) {
                speedBuffMultiplier = 1.0f;
                System.out.println("Speed buff ended");
            }
        }

        if (damageColorTimer > 0) {
            damageColorTimer -= delta;
        }

        damageCooldownTimer = Math.max(0f, damageCooldownTimer - delta);
        if (isHurt) {
            hurtTimer -= delta;
            if (hurtTimer <= 0) isHurt = false;
        }

        float speedMultiplier = 1.0f;
        if (stats != null && stats.getSkillTree() != null) {
            speedMultiplier += stats.getSkillTree().getTotalSpeedBonus();
        }

        float currentSpeed = speed * (run ? runMultiplier : 1f) * speedMultiplier * speedBuffMultiplier;

        velocity.set(0, 0);

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

        float oldX = hitbox.x;
        hitbox.x += velocity.x * delta;
        if (walls != null) {
            for (Wall wall : walls) {
                if (hitbox.overlaps(wall.getBounds())) {
                    hitbox.x = oldX;
                    break;
                }
            }
        }

        float oldY = hitbox.y;
        hitbox.y += velocity.y * delta;
        if (walls != null) {
            for (Wall wall : walls) {
                if (hitbox.overlaps(wall.getBounds())) {
                    hitbox.y = oldY;
                    break;
                }
            }
        }

        this.position.set(hitbox.x, hitbox.y);

        if (position.x < 0) position.x = 0;
        if (position.y < 0) position.y = 0;
        hitbox.setPosition(position.x, position.y);
    }

    public void triggerDamageVFX() {
        this.damageColorTimer = 1.0f;
    }

    public void render(SpriteBatch batch) {
        if (currentFrame == null) return;

        if (isInvincible) {
            float alpha = (float) (Math.sin(blinkTimer) * 0.5 + 0.5);
            batch.setColor(1.0f, 1.0f, 0.5f, alpha);
        }
        else if (damageColorTimer > 0) {
            batch.setColor(Color.RED);
        }
        else {
            batch.setColor(Color.WHITE);
        }

        float drawWidth = 32f;
        float drawHeight = 64f;
        float drawX = position.x - (drawWidth - hitbox.width) / 2f;
        float drawY = position.y;

        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
    }

    public void takeDamage(float dmg) {
        if (isInvincible) {
            System.out.println("Invincible! Damage ignored: " + dmg);
            return;
        }

        if (getHealth() <= 0) return;

        if (damageCooldownTimer > 0f) return;

        stats.takeDamage(Math.round(dmg));
        isHurt = true;
        hurtTimer = 0.25f;
        damageCooldownTimer = DAMAGE_COOLDOWN;
        triggerDamageVFX();
    }


    public void healByPercentage(float percentage) {
        if (stats == null) return;

        int maxHealth = stats.getMaxHealth();
        int healAmount = (int)(maxHealth * percentage);

        stats.heal(healAmount);
        System.out.println("Healed by " + (percentage * 100) + "% (" + healAmount + " HP)");
    }


    public void applySpeedBuff(float multiplier, float duration) {
        this.speedBuffMultiplier = multiplier;
        this.speedBuffTimer = duration;
        System.out.println("Speed buff applied: " + multiplier + "x for " + duration + " seconds");
    }

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

    public void enableInvincibility(float duration) {
        this.isInvincible = true;
        this.invincibleTimer = duration;
        this.blinkTimer = 0f;
        System.out.println("Invincibility enabled for " + duration + " seconds!");
    }

    public void enableFatalProtection() {
        enableInvincibility(INVINCIBLE_DURATION);
    }

    public void quickInvincibility(float duration) {
        if (!isInvincible || invincibleTimer < duration) {
            enableInvincibility(duration);
        }
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public float getInvincibleTimeLeft() {
        return Math.max(0f, invincibleTimer);
    }

    public void cancelInvincibility() {
        this.isInvincible = false;
        this.invincibleTimer = 0f;
    }

    public void autoQuickInvincibility() {
        if (!isInvincible) {
            quickInvincibility(0.3f); // 0.3秒短暂无敌
        }
    }

    public float getSpeedBuffMultiplier() {
        return speedBuffMultiplier;
    }

    public float getSpeedBuffTimeLeft() {
        return Math.max(0f, speedBuffTimer);
    }
}
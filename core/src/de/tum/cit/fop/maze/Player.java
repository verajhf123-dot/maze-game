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

    private boolean isHurt = false;
    private float hurtTimer = 0;

    private Rectangle hitbox;

    private PlayerStats stats;
    private float damageCooldownTimer = 0f;
    private static final float DAMAGE_COOLDOWN = 0.5f; // 0.5秒内只吃一次伤害
    private float damageColorTimer = 0f;

    private float speedBuffTimer = 0f;       // Buff 剩余时间
    private float speedBuffMultiplier = 1.0f; // 当前的加速倍率 (1.0表示正常)

    private boolean isInvincible = false;
    private float invincibleTimer = 0f;


    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private float attackCooldownTimer = 0f;
    private static final float ATTACK_DURATION = 0.2f; // 攻击判定存在 0.2秒
    private static final float ATTACK_COOLDOWN = 0.5f; // 0.5秒攻击一次
    private Rectangle attackHitbox; // 攻击判定框
    private int facingDirection = 0; // 0=下, 1=右, 2=上, 3=左




    public Player(float x, float y,PlayerStats inheritedStats) {
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


        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.stats = new PlayerStats();
        this.hitbox = new Rectangle(x, y, 14, 14);


        this.attackHitbox = new Rectangle(0, 0, 40, 40);

        if (inheritedStats != null) {
            this.stats = inheritedStats;
            this.stats.setPlayer(this); // 重要：更新 Stats 里的 player 引用
            System.out.println("Player inherited stats! Level: " + stats.getExpSystem().getCurrentLevel());
        } else {
            this.stats = new PlayerStats();
            this.stats.setPlayer(this); // 记得也要初始化 skillManager
        }

        System.out.println("Texture size: " + texture.getWidth() + "x" + texture.getHeight());
        System.out.println("Split rows: " + tmp.length + ", cols: " + tmp[0].length);
        System.out.println("Frame size: 16x32");
        System.out.println("Width%16=" + (texture.getWidth() % 16) + ", Height%32=" + (texture.getHeight() % 32));




    }



    public void update(float delta, boolean up, boolean down, boolean left, boolean right, boolean run, List<Wall> walls) {
        if (damageColorTimer > 0) {
            damageColorTimer -= delta;
        }
        damageCooldownTimer = Math.max(0f, damageCooldownTimer - delta);
        if (isHurt) {
            hurtTimer -= delta;
            if (hurtTimer <= 0) isHurt = false;
        }


        if (isInvincible) {
            invincibleTimer -= delta;
            if (invincibleTimer <= 0) {
                isInvincible = false; // 时间到，取消无敌
                System.out.println("Jingangfu expired.");
            }
        }


        if (speedBuffTimer > 0) {
            speedBuffTimer -= delta;
            if (speedBuffTimer <= 0) {
                speedBuffMultiplier = 1.0f; // 时间到，恢复正常
                System.out.println("Speed Buff Ended.");
            }
        }

        if (attackCooldownTimer > 0) attackCooldownTimer -= delta;
        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0) isAttacking = false;
        }


        float speedMultiplier = 1.0f;
        if (stats != null && stats.getSkillTree() != null) {
            speedMultiplier += stats.getSkillTree().getTotalSpeedBonus();
        }

        float currentSpeed = speed * (run ? runMultiplier : 1f) * speedMultiplier*this.speedBuffMultiplier ;

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
        float oldX = hitbox.x;
        hitbox.x += velocity.x * delta;
        if (walls != null) {
            for (Wall wall : walls) {
                if (hitbox.overlaps(wall.getBounds())) {
                    hitbox.x = oldX; // 撞墙，退回
                    break;
                }
            }
        }

        float oldY = hitbox.y;
        hitbox.y += velocity.y * delta;
        if (walls != null) {
            for (Wall wall : walls) {
                if (hitbox.overlaps(wall.getBounds())) {
                    hitbox.y = oldY; // 撞墙，退回
                    break;
                }
            }
        }

        this.position.set(hitbox.x, hitbox.y);

        // 4. 边界检查
        if (position.x < 0) position.x = 0;
        if (position.y < 0) position.y = 0;
        hitbox.setPosition(position.x, position.y);

        updateAttackHitboxPosition();


    }

    public void performAttack() {
        if (attackCooldownTimer <= 0 && !isAttacking) {
            isAttacking = true;
            attackTimer = ATTACK_DURATION;
            attackCooldownTimer = ATTACK_COOLDOWN;
            System.out.println("Player Attacked! Dir: " + facingDirection);
        }
    }


    private void updateAttackHitboxPosition() {
        float range = 60f;
        float pX = hitbox.x + hitbox.width / 2;
        float pY = hitbox.y + hitbox.height / 2;

        attackHitbox.set(
                pX - range / 2,
                pY - range / 2,
                range,
                range
        );
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
        float drawX = position.x - (drawWidth - hitbox.width) / 2f;
        float drawY = position.y;

        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);

    }




    public void takeDamage(float dmg) {
        if (getHealth() <= 0) return;

        if (isInvincible) {
            System.out.println("Damage Blocked by Jingangfu!");
            return;
        }

        if (damageCooldownTimer > 0f) return;
        stats.takeDamage(Math.round(dmg));
        isHurt = true;
        hurtTimer = 0.25f;
        damageCooldownTimer = DAMAGE_COOLDOWN;
        triggerDamageVFX();



    }


    public void heal(float amount) {
        if (stats != null) {
            stats.heal((int) amount);
        }
    }

    public void healByPercentage(float percentage) {
        if (stats != null) {
            float amount = stats.getMaxHealth() * percentage;
            heal(amount);
        }
    }


    public void applySpeedBuff(float multiplier, float duration) {
        this.speedBuffMultiplier = multiplier; // 设置倍率 (比如 1.2)
        this.speedBuffTimer = duration;        // 设置持续时间 (比如 20秒)
        System.out.println("Speed Buff Activated! Speed x" + multiplier);
    }



    public void enableFatalProtection() {
        this.isInvincible = true;
        this.invincibleTimer = 10.0f; // 设定无敌时间，例如 10 秒
        System.out.println("Jingangfu Activated! You are INVINCIBLE!");
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

    public String getDebugInfo() {
        return "Player pos: (" + position.x + "," + position.y +
                "), hitbox: " + hitbox +
                ", health: " + getHealth();
    }

    public void dispose() {
        texture.dispose();
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }
}




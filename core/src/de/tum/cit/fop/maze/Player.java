package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class Player implements CollidableEntity {

    private Texture texture;
    private TextureRegion currentFrame;

    private Animation<TextureRegion> walkDownAnim, walkUpAnim, walkLeftAnim, walkRightAnim;
    private Animation<TextureRegion> attackDownAnim, attackUpAnim, attackLeftAnim, attackRightAnim;

    private float stateTime = 0f;
    private Vector2 position;
    private Vector2 velocity;

    private float speed = 120f;
    private float runMultiplier = 1.6f;

    private boolean isHurt = false;
    private float hurtTimer = 0;

    private Rectangle hitbox;
    private PlayerStats stats;
    private float damageCooldownTimer = 0f;
    private static final float DAMAGE_COOLDOWN = 0.5f;
    private float damageColorTimer = 0f;

    private float speedBuffTimer = 0f;
    private float speedBuffMultiplier = 1.0f;

    private boolean isInvincible = false;
    private float invincibleTimer = 0f;

    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private float attackCooldownTimer = 0f;

    private static final float ATTACK_DURATION = 0.4f;
    private static final float ATTACK_COOLDOWN = 0.5f;

    private Rectangle attackHitbox;
    private int facingDirection = 0; // 0=Down, 1=Right, 2=Up, 3=Left

    public Player(float x, float y, PlayerStats inheritedStats) {
        this.texture = new Texture("character.png");
        TextureRegion[][] tmpWalk = TextureRegion.split(texture, 16, 32);
        TextureRegion[][] tmpAttack = TextureRegion.split(texture, 34, 32);

        if (tmpWalk.length >= 4) {
            walkDownAnim  = new Animation<>(0.15f, tmpWalk[0][0], tmpWalk[0][1], tmpWalk[0][2]);
            walkRightAnim = new Animation<>(0.15f, tmpWalk[1][0], tmpWalk[1][1], tmpWalk[1][2]);
            walkUpAnim    = new Animation<>(0.15f, tmpWalk[2][0], tmpWalk[2][1], tmpWalk[2][2]);
            walkLeftAnim  = new Animation<>(0.15f, tmpWalk[3][0], tmpWalk[3][1], tmpWalk[3][2]);
            walkDownAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkRightAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkUpAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkLeftAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }

        if (tmpAttack.length >= 8) {
            attackDownAnim  = new Animation<>(0.1f, tmpAttack[4][0], tmpAttack[4][1], tmpAttack[4][2], tmpAttack[4][3]);
            attackRightAnim = new Animation<>(0.1f, tmpAttack[5][0], tmpAttack[5][1], tmpAttack[5][2], tmpAttack[5][3]);
            attackUpAnim    = new Animation<>(0.1f, tmpAttack[6][0], tmpAttack[6][1], tmpAttack[6][2], tmpAttack[6][3]);
            attackLeftAnim  = new Animation<>(0.1f, tmpAttack[7][0], tmpAttack[7][1], tmpAttack[7][2], tmpAttack[7][3]);
        }

        if (walkDownAnim == null) {
            Animation<TextureRegion> def = new Animation<>(0.1f, new TextureRegion(texture));
            walkDownAnim = def; walkRightAnim = def; walkUpAnim = def; walkLeftAnim = def;
            attackDownAnim = def; attackRightAnim = def; attackUpAnim = def; attackLeftAnim = def;
        }

        this.currentFrame = walkDownAnim.getKeyFrame(0);
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.hitbox = new Rectangle(x, y, 14, 14);
        this.attackHitbox = new Rectangle(0, 0, 40, 40);

        if (inheritedStats != null) {
            this.stats = inheritedStats;
        } else {
            this.stats = new PlayerStats();
        }
        this.stats.setPlayer(this);
    }

    public void update(float delta, boolean up, boolean down, boolean left, boolean right, boolean run, List<Wall> walls) {
        // === 1. 关键：更新 Stats，让技能冷却时间走动 ===
        if (stats != null) {
            stats.update(delta);
        }

        if (damageColorTimer > 0) damageColorTimer -= delta;
        damageCooldownTimer = Math.max(0f, damageCooldownTimer - delta);
        if (isHurt) {
            hurtTimer -= delta;
            if (hurtTimer <= 0) isHurt = false;
        }
        if (isInvincible) {
            invincibleTimer -= delta;
            if (invincibleTimer <= 0) isInvincible = false;
        }
        if (speedBuffTimer > 0) {
            speedBuffTimer -= delta;
            if (speedBuffTimer <= 0) speedBuffMultiplier = 1.0f;
        }
        if (attackCooldownTimer > 0) attackCooldownTimer -= delta;
        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0) isAttacking = false;
        }

        // === 2. 关键：速度加成逻辑 ===
        float totalSpeedMultiplier = 1.0f;
        if (run) totalSpeedMultiplier *= runMultiplier;
        totalSpeedMultiplier *= this.speedBuffMultiplier;

        // 读取技能树的速度加成
        if (stats != null && stats.getSkillTree() != null) {
            totalSpeedMultiplier += stats.getSkillTree().getTotalSpeedBonus();
        }

        float currentSpeed = speed * totalSpeedMultiplier;

        velocity.set(0, 0);
        boolean isMoving = false;

        if (up) { velocity.y = currentSpeed; facingDirection = 2; isMoving = true; }
        else if (down) { velocity.y = -currentSpeed; facingDirection = 0; isMoving = true; }

        if (left) { velocity.x = -currentSpeed; facingDirection = 3; isMoving = true; }
        else if (right) { velocity.x = currentSpeed; facingDirection = 1; isMoving = true; }

        if (isAttacking) {
            stateTime += delta;
        } else if (isMoving) {
            stateTime += delta;
        } else {
            stateTime = 0;
        }

        Animation<TextureRegion> currentAnim;
        if (isAttacking) {
            switch (facingDirection) {
                case 1: currentAnim = attackRightAnim; break;
                case 2: currentAnim = attackUpAnim; break;
                case 3: currentAnim = attackLeftAnim; break;
                default: currentAnim = attackDownAnim; break;
            }
            currentFrame = currentAnim.getKeyFrame(stateTime, true);
        } else {
            switch (facingDirection) {
                case 1: currentAnim = walkRightAnim; break;
                case 2: currentAnim = walkUpAnim; break;
                case 3: currentAnim = walkLeftAnim; break;
                default: currentAnim = walkDownAnim; break;
            }
            currentFrame = currentAnim.getKeyFrame(stateTime, true);
        }

        float oldX = hitbox.x;
        hitbox.x += velocity.x * delta;
        if (walls != null) {
            for (Wall wall : walls) {
                if (hitbox.overlaps(wall.getBounds())) { hitbox.x = oldX; break; }
            }
        }

        float oldY = hitbox.y;
        hitbox.y += velocity.y * delta;
        if (walls != null) {
            for (Wall wall : walls) {
                if (hitbox.overlaps(wall.getBounds())) { hitbox.y = oldY; break; }
            }
        }

        this.position.set(hitbox.x, hitbox.y);
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
        }
    }

    private void updateAttackHitboxPosition() {
        float range = 60f;
        float pX = hitbox.x + hitbox.width / 2;
        float pY = hitbox.y + hitbox.height / 2;
        attackHitbox.set(pX - range / 2, pY - range / 2, range, range);
    }

    public void triggerDamageVFX() { this.damageColorTimer = 1.0f; }

    public void render(SpriteBatch batch) {
        if (currentFrame == null) return;
        if (damageColorTimer > 0) batch.setColor(Color.RED);
        else batch.setColor(Color.WHITE);

        float drawWidth = currentFrame.getRegionWidth() * 2f;
        float drawHeight = currentFrame.getRegionHeight() * 2f;
        float drawX = position.x - (drawWidth - hitbox.width) / 2f;
        float drawY = position.y;

        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
    }

    // 在 Player.java 中修改这个方法

    public void takeDamage(float dmg) {
        if (getHealth() <= 0) return;
        if (isInvincible) return;
        if (damageCooldownTimer > 0f) return;

        // === 修改后 (正确) ===
        // 显式传递 "物理伤害" 类型
        if (stats != null) {
            int oldHealth = stats.getHealth();

            // ▼▼▼ 重点修改了这一行 ▼▼▼
            stats.takeDamage(dmg, PlayerStats.DAMAGE_TYPE_PHYSICAL);

            // 只有真的掉血了才硬直
            if (stats.getHealth() < oldHealth) {
                isHurt = true;
                hurtTimer = 0.25f;
                damageCooldownTimer = DAMAGE_COOLDOWN;
                triggerDamageVFX();
            }
        }
    }

    public void heal(float amount) { if (stats != null) stats.heal((int) amount); }
    public void healByPercentage(float percentage) { if (stats != null) heal(stats.getMaxHealth() * percentage); }
    public void applySpeedBuff(float multiplier, float duration) { this.speedBuffMultiplier = multiplier; this.speedBuffTimer = duration; }
    public void enableFatalProtection() { this.isInvincible = true; this.invincibleTimer = 10.0f; }

    public float getHealth() { return stats != null ? stats.getHealth() : 0; }
    public float getMaxHealth() { return stats != null ? stats.getMaxHealth() : 100; }
    public Vector2 getVelocity() { return velocity; }
    public void syncPositionToHitbox() { this.position.set(hitbox.x, hitbox.y); }
    public Rectangle getHitbox() { return hitbox; }
    public PlayerStats getStats() { return stats; }
    public com.badlogic.gdx.math.Vector2 getPosition() { return position; }
    public void dispose() { texture.dispose(); }
    public boolean isAttacking() { return isAttacking; }
    public Rectangle getAttackHitbox() { return attackHitbox; }
}
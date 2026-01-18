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

    // 0.4秒播放完4帧攻击动画
    private static final float ATTACK_DURATION = 0.4f;
    private static final float ATTACK_COOLDOWN = 0.5f;

    private Rectangle attackHitbox;
    private int facingDirection = 0;

    public Player(float x, float y, PlayerStats inheritedStats) {
        this.texture = new Texture("character.png");
        this.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);



        // === 🔥 核心修改：分开切图 🔥 ===

        // 1. 行走图：按 16x32 切割 (解决行走鬼影问题)
        TextureRegion[][] tmpWalk = TextureRegion.split(texture, 16, 32);

        // 2. 攻击图：按 34x32 切割 (解决攻击被截断问题)
        TextureRegion[][] tmpAttack = TextureRegion.split(texture, 34, 32);

        // --- A. 初始化行走动画 (使用 tmpWalk, 前4行) ---
        if (tmpWalk.length >= 4) {
            // 注意：行走通常取前3帧 (0, 1, 2)
            walkDownAnim  = new Animation<>(0.15f, tmpWalk[0][0], tmpWalk[0][1], tmpWalk[0][2]);
            walkRightAnim = new Animation<>(0.15f, tmpWalk[1][0], tmpWalk[1][1], tmpWalk[1][2]);
            walkUpAnim    = new Animation<>(0.15f, tmpWalk[2][0], tmpWalk[2][1], tmpWalk[2][2]);
            walkLeftAnim  = new Animation<>(0.15f, tmpWalk[3][0], tmpWalk[3][1], tmpWalk[3][2]);

            walkDownAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkRightAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkUpAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkLeftAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }

        // --- B. 初始化攻击动画 (使用 tmpAttack, 后4行) ---
        if (tmpAttack.length >= 8) {
            // 注意：攻击图在第 4,5,6,7 行
            attackDownAnim  = new Animation<>(0.1f, tmpAttack[4][0], tmpAttack[4][1], tmpAttack[4][2], tmpAttack[4][3]);
            attackRightAnim = new Animation<>(0.1f, tmpAttack[5][0], tmpAttack[5][1], tmpAttack[5][2], tmpAttack[5][3]);
            attackUpAnim    = new Animation<>(0.1f, tmpAttack[6][0], tmpAttack[6][1], tmpAttack[6][2], tmpAttack[6][3]);
            attackLeftAnim  = new Animation<>(0.1f, tmpAttack[7][0], tmpAttack[7][1], tmpAttack[7][2], tmpAttack[7][3]);
        }

        // 保底初始化
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
            this.stats.setPlayer(this);
        } else {
            this.stats = new PlayerStats();
            this.stats.setPlayer(this);
        }
    }

    public void update(float delta, boolean up, boolean down, boolean left, boolean right, boolean run, List<Wall> walls) {
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

        float speedMultiplier = 1.0f;
        if (stats != null && stats.getSkillTree() != null) {
            speedMultiplier += stats.getSkillTree().getTotalSpeedBonus();
        }

        float currentSpeed = speed * (run ? runMultiplier : 1f) * speedMultiplier * this.speedBuffMultiplier;

        velocity.set(0, 0);
        boolean isMoving = false;

        if (up) { velocity.y = currentSpeed; facingDirection = 2; isMoving = true; }
        else if (down) { velocity.y = -currentSpeed; facingDirection = 0; isMoving = true; }

        if (left) { velocity.x = -currentSpeed; facingDirection = 3; isMoving = true; }
        else if (right) { velocity.x = currentSpeed; facingDirection = 1; isMoving = true; }

        // 动画时间更新
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
            System.out.println("Player Attacked! Dir: " + facingDirection);
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

        // === 🔥 核心修复：根据当前帧的宽度动态调整绘制大小 🔥 ===
        // 这样一来：
        // 1. 如果是走路帧 (宽16)，画出来就是 32 (不会拉宽)
        // 2. 如果是攻击帧 (宽34)，画出来就是 68 (不会截断)
        // 使用 2倍 放大 (Scale 2.0)

        float drawWidth = currentFrame.getRegionWidth() * 2f;
        float drawHeight = currentFrame.getRegionHeight() * 2f;

        float drawX = position.x - (drawWidth - hitbox.width) / 2f;
        float drawY = position.y;

        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
    }

    public void takeDamage(float dmg) {
        if (getHealth() <= 0) return;
        if (isInvincible) return;
        if (damageCooldownTimer > 0f) return;
        stats.takeDamage(Math.round(dmg));
        isHurt = true;
        hurtTimer = 0.25f;
        damageCooldownTimer = DAMAGE_COOLDOWN;
        triggerDamageVFX();


        System.out.println("WALK:  " + texture.getWidth() + "x" + texture.getHeight() +
                "  mod16=" + (texture.getWidth()%16) + " mod32=" + (texture.getHeight()%32));

        System.out.println("ATTACK: mod34=" + (texture.getWidth()%34));

    }
    public void heal(float amount) { if (stats != null) stats.heal((int) amount); }
    public void healByPercentage(float percentage) { if (stats != null) heal(stats.getMaxHealth() * percentage); }
    public void applySpeedBuff(float multiplier, float duration) { this.speedBuffMultiplier = multiplier; this.speedBuffTimer = duration; }
    public void enableFatalProtection() { this.isInvincible = true; this.invincibleTimer = 10.0f; }
    public float getHealth() { return stats.getHealth(); }
    public float getMaxHealth() { return stats.getMaxHealth(); }
    public Vector2 getVelocity() { return velocity; }
    public void syncPositionToHitbox() { this.position.set(hitbox.x, hitbox.y); }
    public Rectangle getHitbox() { return hitbox; }
    public PlayerStats getStats() { return stats; }
    public com.badlogic.gdx.math.Vector2 getPosition() { return position; }
    public void dispose() { texture.dispose(); }
    public boolean isAttacking() { return isAttacking; }
    public Rectangle getAttackHitbox() { return attackHitbox; }
}
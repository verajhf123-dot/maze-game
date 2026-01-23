package de.tum.cit.fop.maze;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * Represents the player character.
 *
 * The player stores movement state (position, velocity, facing) and combat state
 * (health/stats, attack and hurt flags, cooldown timers). Each frame it updates
 * its animation frame and collision/attack hitboxes, and it can be rendered with SpriteBatch.
 *
 * The player is updated by GameScreen and interacts with walls, enemies, items and skills.
 */


public class Player implements CollidableEntity {

    private Texture texture;
    private TextureRegion currentFrame;

    private Animation<TextureRegion> walkDown, walkUp, walkLeft, walkRight;
    private Animation<TextureRegion> attackDown, attackUp, attackLeft, attackRight;

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

    private boolean invulnerable = false;
    private float invulnTime = 0f;

    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private float attackCooldownTimer = 0f;

    private static final float ATTACK_DURATION = 0.4f;
    private static final float ATTACK_COOLDOWN = 0.5f;

    private Rectangle attackHitbox;
    private int facingDirection = 0;

    /**
     * Create a player at the given start position and initializes animations.
     * The texture is split into walk and attack frames.If splitting fails,a default frame is used.
     * @param x start x position in world coordinates
     * @param y start y position in world coordinates
     * @param inheritedStats optional stats carried over from a previous level
     */

    public Player(float x, float y, PlayerStats inheritedStats) {
        this.texture = new Texture("character.png");
        TextureRegion[][] tmpWalk = TextureRegion.split(texture, 64, 128);
        TextureRegion[][] tmpAttack = TextureRegion.split(texture, 136, 128);

        if (tmpWalk.length >= 4) {
            walkDown = new Animation<>(0.15f, tmpWalk[0][0], tmpWalk[0][1], tmpWalk[0][2]);
            walkRight = new Animation<>(0.15f, tmpWalk[1][0], tmpWalk[1][1], tmpWalk[1][2]);
            walkUp = new Animation<>(0.15f, tmpWalk[2][0], tmpWalk[2][1], tmpWalk[2][2]);
            walkLeft = new Animation<>(0.15f, tmpWalk[3][0], tmpWalk[3][1], tmpWalk[3][2]);
            walkDown.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkRight.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkUp.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
            walkLeft.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        }

        if (tmpAttack.length >= 8) {
            attackDown  = new Animation<>(0.1f, tmpAttack[4][0], tmpAttack[4][1], tmpAttack[4][2], tmpAttack[4][3]);
            attackRight = new Animation<>(0.1f, tmpAttack[5][0], tmpAttack[5][1], tmpAttack[5][2], tmpAttack[5][3]);
            attackUp = new Animation<>(0.1f, tmpAttack[6][0], tmpAttack[6][1], tmpAttack[6][2], tmpAttack[6][3]);
            attackLeft = new Animation<>(0.1f, tmpAttack[7][0], tmpAttack[7][1], tmpAttack[7][2], tmpAttack[7][3]);
        }

        if (walkDown == null) {
            Animation<TextureRegion> def = new Animation<>(0.1f, new TextureRegion(texture));
            walkDown = def; walkRight = def; walkUp = def; walkLeft = def;
            attackDown = def; attackRight = def; attackUp = def; attackLeft = def;
        }

        this.currentFrame = walkDown.getKeyFrame(0);
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

    /**
     *update the player for one frame:movement,timers(hurt/invuln/buffs/attack),and animation state.
     * Also resolves basic wall collisions by reverting x/y movement if the hitbox overlaps a wall.
     *params all from the GameScreen
     *  @param delta time passed since the last frame (seconds)
     *  @param up whether the player is moving up
     *  @param down whether the player is moving down
     *  @param left whether the player is moving left
     *  @param right whether the player is moving right
     *  @param run whether the run key is held (applies runMultiplier)
     *  @param walls list of walls used for simple collision checks (can be null)
     *
     *
     */

    public void update(float delta, boolean up, boolean down, boolean left, boolean right, boolean run, List<Wall> walls) {
        if (stats != null) {
            stats.update(delta);
        }

        if (damageColorTimer > 0) damageColorTimer -= delta;
        damageCooldownTimer = Math.max(0f, damageCooldownTimer - delta);
        if (isHurt) {
            hurtTimer -= delta;
            if (hurtTimer <= 0) isHurt = false;
        }
        if (invulnerable) {
            invulnTime -= delta;
            if (invulnTime <= 0) invulnerable = false;
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

        float totalSpeedMultiplier = 1.0f;
        if (run) totalSpeedMultiplier *= runMultiplier;
        totalSpeedMultiplier *= this.speedBuffMultiplier;

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
                case 1: currentAnim = attackRight; break;
                case 2: currentAnim = attackUp; break;
                case 3: currentAnim = attackLeft; break;
                default: currentAnim = attackDown; break;
            }
            currentFrame = currentAnim.getKeyFrame(stateTime, true);
        } else {
            switch (facingDirection) {
                case 1: currentAnim = walkRight; break;
                case 2: currentAnim = walkUp; break;
                case 3: currentAnim = walkLeft; break;
                default: currentAnim = walkDown; break;
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

    /**Stats an attack if the player is not already attacking and the attack
     * has finished.
     *
     */

    public void performAttack() {
        if (attackCooldownTimer <= 0 && !isAttacking) {
            isAttacking = true;
            attackTimer = ATTACK_DURATION;
            attackCooldownTimer = ATTACK_COOLDOWN;
        }
    }

    /**Updates the attack hitbox position so
     * it is centered around the player.
     *
     */

    private void updateAttackHitboxPosition() {
        float range = 60f;
        float pX = hitbox.x + hitbox.width / 2;
        float pY = hitbox.y + hitbox.height / 2;
        attackHitbox.set(pX - range / 2, pY - range / 2, range, range);
    }

    /**
     *Triggers the damage visual
     * effect by resetting the damage color timer.
     *
     */

    public void triggerDamageVFX() {
        this.damageColorTimer = 1.0f;
    }

    /**Renders the Player using the current animation frame.
     * If the player was recently damaged, the sprite is drawn red for a short time.
     *
     * @param batch SpriteBach used to draw the player texture region.
     */

    public void render(SpriteBatch batch) {
        if (currentFrame == null) return;
        if (damageColorTimer > 0) batch.setColor(Color.RED);
        else batch.setColor(Color.WHITE);

        float drawWidth = currentFrame.getRegionWidth() * 0.5f;
        float drawHeight = currentFrame.getRegionHeight() * 0.5f;
        float drawX = position.x - (drawWidth - hitbox.width) / 2f;
        float drawY = position.y;

        batch.draw(currentFrame, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
    }

    /**
     *Check whether damage is allowed,
     * ignored when player is dead,invulnerable and still in cooldowntime,
     * if successful,updates the state(hurTimer,cooldownTimer and VFX)
     *
     * @param dmg amount of damage to apply
     */

    public void takeDamage(float dmg) {
        if (getHealth() <= 0) return;
        if (invulnerable) return;
        if (damageCooldownTimer > 0f) return;
        if (stats != null) {
            int oldHealth = stats.getHealth();
            stats.takeDamage(dmg, PlayerStats.DAMAGE_TYPE_PHYSICAL);
            if (stats.getHealth() < oldHealth) {
                isHurt = true;
                hurtTimer = 0.25f;
                damageCooldownTimer = DAMAGE_COOLDOWN;
                triggerDamageVFX();
            }
        }
    }

    /**
     *Heals the player by a fixed amount
     * @param amount heal amount
     */

    public void heal(float amount) {
        if (stats != null) stats.heal((int) amount);
    }

    /**
     * Heals the player by a percentage of max health.
     * @param percentage
     */

    public void healByPercentage(float percentage) {
        if (stats != null) heal(stats.getMaxHealth() * percentage);
    }

    /**
     * Applies a temporary speed multiplier.
     * @param multiplier speed factor
     * @param duration duration in seconds
     */

    public void applySpeedBuff(float multiplier, float duration) {
        this.speedBuffMultiplier = multiplier;
        this.speedBuffTimer = duration;
    }

    /**
     * Enable a temporary invulnerability period(fatal protection).
     * uses a fixed duration.
     */

    public void enableFatalProtection() {
        this.invulnerable = true; this.invulnTime = 10.0f;
    }

    public float getHealth() {
        return stats != null ? stats.getHealth() : 0;
    }
    public float getMaxHealth() {
        return stats != null ? stats.getMaxHealth() : 100;
    }
    public Vector2 getVelocity() {
        return velocity;
    }

    /**
     * Syncs the visual position to match the hitbox position after collision adjustments.
     */
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

    /**
     * Disposes texture owned vy player to free GPU resources.
     */

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
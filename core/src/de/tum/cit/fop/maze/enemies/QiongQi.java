package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.Wall;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class QiongQi extends Enemy {

    // Possible movement directions
    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private Map<Direction, Animation<TextureRegion>> walkAnimations;
    private Map<Direction, TextureRegion> idleFrames;

    private Direction currentDirection = Direction.DOWN;
    private float stateTime = 0f;           // Timer for animations
    private float animationSpeed = 0.15f;

    // Attack flags and cooldowns
    private boolean isAttacking = false;
    private float attackCooldown = 1.2f;
    private float currentAttackCooldown = 0f;

    // Charge-specific fields
    private boolean isCharging = false;
    private float chargeSpeed = 250f;
    private float normalSpeed;
    private float chargeCooldown = 8f;
    private float currentChargeCooldown = 0f;
    private Vector2 chargeDirection;
    private float chargeDuration = 1.5f;
    private float chargeTimer = 0f;

    public QiongQi(float x, float y) {
        super(x, y, 30, 30);

        // Initialize stats
        this.normalSpeed = 70f;
        this.speed = normalSpeed;
        this.maxHealth = 100f;
        this.health = maxHealth;
        this.attackDamage = 12f;
        this.attackRange = 45f;
        this.detectionRange = 180f;

        // Load animations
        initializeAnimations();

        System.out.println("QiongQi initialized (3 frames per direction)");
    }

    // ------------------- Animation Initialization -------------------
    private void initializeAnimations() {
        walkAnimations = new HashMap<>();
        idleFrames = new HashMap<>();

        loadDirection("down", Direction.DOWN);
        loadDirection("up", Direction.UP);
        loadDirection("left", Direction.LEFT);
        loadDirection("right", Direction.RIGHT);
    }

    private void loadDirection(String basePath, Direction dir) {
        TextureRegion[] frames = loadDirectionFrames("enemies/qiongqi/" + basePath + "_", 3);
        if (frames[0] != null) {
            walkAnimations.put(dir, new Animation<>(animationSpeed, frames));
            idleFrames.put(dir, frames[1]); // Middle frame as idle
        }
    }

    private TextureRegion[] loadDirectionFrames(String basePath, int frameCount) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            try {
                String path = basePath + (i + 1) + ".png";
                if (Gdx.files.internal(path).exists()) {
                    Texture texture = new Texture(Gdx.files.internal(path));
                    frames[i] = new TextureRegion(texture);
                } else {
                    frames[i] = null;
                }
            } catch (Exception e) {
                frames[i] = null;
            }
        }
        return frames;
    }

    private void loadFallbackTexture() {
        try {
            Texture fallback = new Texture(Gdx.files.internal("enemies/qiongqi.png"));
            TextureRegion frame = new TextureRegion(fallback);
            Animation<TextureRegion> singleAnim = new Animation<>(1f, frame);

            for (Direction dir : Direction.values()) {
                walkAnimations.put(dir, singleAnim);
                idleFrames.put(dir, frame);
            }

            System.out.println("Using fallback texture for QiongQi");
        } catch (Exception e) {
            System.out.println("No fallback texture available for QiongQi");
        }
    }

    // ------------------- Update Logic -------------------
    @Override
    public void update(float delta, List<Wall> walls) {
        super.update(delta, walls);

        stateTime += delta;

        // Reduce attack and charge cooldowns
        if (currentAttackCooldown > 0) currentAttackCooldown -= delta;
        if (currentChargeCooldown > 0) currentChargeCooldown -= delta;

        // Handle charging movement
        if (isCharging) {
            chargeTimer -= delta;
            if (chargeTimer <= 0) {
                endCharge();
            } else {
                performCharge(delta);
                return; // Skip normal movement
            }
        }

        updateDirection();

        // Player interaction
        if (player != null) {
            Vector2 playerPos = player.getPosition();
            float distance = position.dst(playerPos);

            // Melee attack
            if (distance <= attackRange && currentAttackCooldown <= 0) {
                performMeleeAttack(player);
                isAttacking = true;
                currentAttackCooldown = attackCooldown;
            }

            // Start charge if close enough and cooldown elapsed
            if (!isCharging && currentChargeCooldown <= 0 && distance < 100f) {
                if (Math.random() < 0.3f) {
                    startCharge(playerPos);
                }
            }
        }
    }

    // ------------------- Direction Update -------------------
    private void updateDirection() {
        if (velocity.len() > 0.1f) {
            float angle = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));

            if (Math.abs(angle) <= 45f) currentDirection = Direction.RIGHT;
            else if (angle > 45f && angle <= 135f) currentDirection = Direction.UP;
            else if (angle < -45f && angle >= -135f) currentDirection = Direction.DOWN;
            else currentDirection = Direction.LEFT;
        }
    }

    // ------------------- Render -------------------
    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive()) return;

        boolean isMoving = velocity.len() > 0.1f || isCharging;
        TextureRegion currentFrame = null;

        Animation<TextureRegion> walkAnim = walkAnimations.get(currentDirection);
        TextureRegion idleFrame = idleFrames.get(currentDirection);

        if (walkAnim != null && idleFrame != null) {
            currentFrame = isMoving ? walkAnim.getKeyFrame(stateTime, true) : idleFrame;
        }

        if (currentFrame != null) {
            float drawWidth = 60f;
            float drawHeight = 60f;
            float offsetX = (bounds.width - drawWidth) / 2;
            float offsetY = (bounds.height - drawHeight) / 2;

            if (isCharging) {
                batch.setColor(1f, 0.8f, 0.8f, 1f); // Tint while charging
                batch.draw(currentFrame, position.x + offsetX, position.y + offsetY, drawWidth, drawHeight);
                batch.setColor(1f, 1f, 1f, 1f);
            } else {
                batch.draw(currentFrame, position.x + offsetX, position.y + offsetY, drawWidth, drawHeight);
            }
        }
    }

    // ------------------- Attacking -------------------
    @Override
    public void attack() {
        if (player != null) performMeleeAttack(player);
    }

    private void performMeleeAttack(Player player) {
        if (player == null) return;

        float distance = position.dst(player.getPosition());
        if (distance <= attackRange) {
            float finalDamage = attackDamage;

            // 15% chance for critical hit
            if (Math.random() < 0.15f) {
                finalDamage *= 1.8f;
                System.out.println("QIONGQI CRITICAL HIT!");
            }

            player.takeDamage(finalDamage);
            System.out.println("[MELEE] QiongQi attacks player for " + finalDamage + " damage");
        }
    }

    // ------------------- Charging -------------------
    private void startCharge(Vector2 targetPos) {
        chargeDirection = new Vector2(targetPos.x - position.x, targetPos.y - position.y).nor();
        isCharging = true;
        chargeTimer = chargeDuration;
        speed = chargeSpeed;
        currentChargeCooldown = chargeCooldown;

        System.out.println("QiongQi starts charging!");
    }

    private void performCharge(float delta) {
        if (chargeDirection != null) {
            velocity.set(chargeDirection.x * speed, chargeDirection.y * speed);
        }
    }

    private void endCharge() {
        isCharging = false;
        speed = normalSpeed;
        velocity.set(0f, 0f);
        System.out.println("QiongQi ends charging");
    }

    // ------------------- Difficulty Adjustment -------------------
    @Override
    public void adjustDifficulty(int level) {
        this.maxHealth = 80 + level * 12;
        this.health = maxHealth;
        this.attackDamage = 12 + level * 2.5f;
        this.speed = 70f + level * 4f;
        this.normalSpeed = speed;
        this.chargeSpeed = 250f + level * 10f;
        this.attackCooldown = Math.max(0.8f, 1.2f - level * 0.06f);
        this.chargeCooldown = Math.max(4f, 8f - level * 0.4f);

        System.out.println("QiongQi adjusted - HP=" + maxHealth + ", DMG=" + attackDamage + ", Charge Speed=" + chargeSpeed);
    }

    // ------------------- Death & State -------------------
    @Override
    protected void onDeath() {
        System.out.println("QiongQi has been defeated!");
    }

    public boolean hasEncounteredPlayer() { return player != null; }
    public boolean isAttacking() { return isAttacking; }
    public boolean isCharging() { return isCharging; }
    public float getChargeCooldown() { return currentChargeCooldown; }

    // ------------------- Dispose -------------------
    public void dispose() {
        for (Animation<TextureRegion> anim : walkAnimations.values()) {
            for (TextureRegion frame : anim.getKeyFrames()) {
                if (frame != null && frame.getTexture() != null) frame.getTexture().dispose();
            }
        }

        for (TextureRegion idleFrame : idleFrames.values()) {
            if (idleFrame != null && idleFrame.getTexture() != null) idleFrame.getTexture().dispose();
        }
    }
}
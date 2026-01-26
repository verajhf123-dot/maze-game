package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.Wall;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * NineTailedFox enemy type.
 * Handles movement, AI updates, animations, melee attacks,
 * and interactions with the player.
 */
public class NineTailedFox extends Enemy {

    /** Movement directions for animation purposes. */
    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private Map<Direction, Animation<TextureRegion>> walkAnimations;
    private Map<Direction, TextureRegion> idleFrames;

    private Direction currentDirection = Direction.DOWN;
    private float stateTime = 0f;
    private float animationSpeed = 0.15f;

    private boolean isAttacking = false;
    private float attackCooldown = 1.0f;
    private float currentAttackCooldown = 0f;

    /** Constructor initializes stats and animations. */
    public NineTailedFox(float x, float y) {
        super(x, y, 30, 30);

        this.speed = 50f;
        this.maxHealth = 40f;
        this.health = maxHealth;
        this.attackDamage = 6f;
        this.attackRange = 40f;
        this.detectionRange = 200f;
        this.attackCooldown = 0.8f;

        initializeAnimations();
        System.out.println("NineTailedFox initialized with melee attack");
    }

    /** Loads walking and idle animations for all directions. */
    private void initializeAnimations() {
        walkAnimations = new HashMap<>();
        idleFrames = new HashMap<>();

        try {
            // Load frames for each direction
            TextureRegion[] downFrames = loadDirectionFrames("enemies/nine_tailed_fox/down_", 3);
            if (downFrames[0] != null) {
                walkAnimations.put(Direction.DOWN, new Animation<>(animationSpeed, downFrames));
                idleFrames.put(Direction.DOWN, downFrames[1]);
            }

            TextureRegion[] upFrames = loadDirectionFrames("enemies/nine_tailed_fox/up_", 3);
            if (upFrames[0] != null) {
                walkAnimations.put(Direction.UP, new Animation<>(animationSpeed, upFrames));
                idleFrames.put(Direction.UP, upFrames[1]);
            }

            TextureRegion[] leftFrames = loadDirectionFrames("enemies/nine_tailed_fox/left_", 3);
            if (leftFrames[0] != null) {
                walkAnimations.put(Direction.LEFT, new Animation<>(animationSpeed, leftFrames));
                idleFrames.put(Direction.LEFT, leftFrames[1]);
            }

            TextureRegion[] rightFrames = loadDirectionFrames("enemies/nine_tailed_fox/right_", 3);
            if (rightFrames[0] != null) {
                walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, rightFrames));
                idleFrames.put(Direction.RIGHT, rightFrames[1]);
            } else if (leftFrames[0] != null) {
                // Mirror left frames if right frames are missing
                TextureRegion[] mirroredFrames = mirrorFrames(leftFrames);
                walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, mirroredFrames));
                idleFrames.put(Direction.RIGHT, mirroredFrames[1]);
            }

            System.out.println("Animations loaded successfully");
        } catch (Exception e) {
            System.out.println("Error loading NineTailedFox animations: " + e.getMessage());
            loadFallbackTexture();
        }
    }

    /** Loads frames for a single direction. */
    private TextureRegion[] loadDirectionFrames(String basePath, int frameCount) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            try {
                String path = basePath + (i + 1) + ".png";
                if (Gdx.files.internal(path).exists()) {
                    frames[i] = new TextureRegion(new Texture(Gdx.files.internal(path)));
                } else frames[i] = null;
            } catch (Exception e) {
                frames[i] = null;
            }
        }
        return frames;
    }

    /** Mirrors frames horizontally. */
    private TextureRegion[] mirrorFrames(TextureRegion[] originalFrames) {
        TextureRegion[] mirrored = new TextureRegion[originalFrames.length];
        for (int i = 0; i < originalFrames.length; i++) {
            if (originalFrames[i] != null) {
                mirrored[i] = new TextureRegion(originalFrames[i]);
                mirrored[i].flip(true, false);
            }
        }
        return mirrored;
    }

    /** Loads a fallback static texture if animations fail. */
    private void loadFallbackTexture() {
        try {
            Texture fallback = new Texture(Gdx.files.internal("enemies/nine_tailed_fox.png"));
            TextureRegion singleFrame = new TextureRegion(fallback);
            Animation<TextureRegion> singleAnim = new Animation<>(1f, singleFrame);

            for (Direction dir : Direction.values()) {
                walkAnimations.put(dir, singleAnim);
                idleFrames.put(dir, singleFrame);
            }

            System.out.println("Using fallback texture");
        } catch (Exception e) {
            System.out.println("No fallback texture available");
        }
    }

    /** Updates position, AI, cooldowns, and attack logic. */
    @Override
    public void update(float delta, List<Wall> walls) {
        super.update(delta, walls);

        stateTime += delta;

        if (currentAttackCooldown > 0) currentAttackCooldown -= delta;

        updateDirection();

        // Perform melee attack if player is in range
        if (targetPlayer != null) {
            float distance = getPosition().dst(targetPlayer.getPosition());
            if (distance <= attackRange && currentAttackCooldown <= 0) {
                performMeleeAttack(targetPlayer);
                isAttacking = true;
                currentAttackCooldown = attackCooldown;
            }
        }
    }

    /** Updates facing direction based on velocity. */
    private void updateDirection() {
        if (velocity.len() > 0.1f) {
            float angle = (float) Math.atan2(velocity.y, velocity.x) * 180f / (float) Math.PI;
            if (Math.abs(angle) <= 45f) currentDirection = Direction.RIGHT;
            else if (angle > 45f && angle <= 135f) currentDirection = Direction.UP;
            else if (angle < -45f && angle >= -135f) currentDirection = Direction.DOWN;
            else currentDirection = Direction.LEFT;
        }
    }

    /** Renders current frame based on movement and direction. */
    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive()) return;

        boolean isMoving = velocity.len() > 0.1f;

        Animation<TextureRegion> walkAnim = walkAnimations.get(currentDirection);
        TextureRegion idleFrame = idleFrames.get(currentDirection);
        TextureRegion currentFrame = (isMoving && walkAnim != null) ? walkAnim.getKeyFrame(stateTime, true) : idleFrame;

        if (currentFrame != null) {
            float drawWidth = 50f;
            float drawHeight = 50f;
            float offsetX = (bounds.width - drawWidth) / 2;
            float offsetY = (bounds.height - drawHeight) / 2;

            batch.draw(currentFrame, position.x + offsetX, position.y + offsetY, drawWidth, drawHeight);
        }
    }

    /** Triggers melee attack on the target player. */
    @Override
    public void attack() {
        if (targetPlayer != null) performMeleeAttack(targetPlayer);
    }

    /** Handles the actual melee attack and damage calculation. */
    private void performMeleeAttack(Player player) {
        float distance = position.dst(player.getPosition());
        if (distance <= attackRange) {
            float finalDamage = attackDamage;
            if (Math.random() < 0.1f) {
                finalDamage *= 1.5f; // Critical hit
                System.out.println("NINE-TAILED FOX CRITICAL HIT!");
            }
            player.takeDamage(finalDamage);
            System.out.println("[MELEE] NineTailedFox attacks player for " + finalDamage + " damage");
            if (currentBehavior != null) currentBehavior.onAttack();
        }
    }

    /** Adjusts stats based on difficulty level. */
    @Override
    public void adjustDifficulty(int level) {
        this.maxHealth = 30 + (level * 8);
        this.health = this.maxHealth;
        this.attackDamage = 8 + (level * 2);
        this.speed = 90f + (level * 5f);
        this.attackCooldown = Math.max(0.6f, 0.8f - (level * 0.05f));

        System.out.println(this.getClass().getSimpleName() +
                " adjusted - HP=" + maxHealth +
                ", MELEE DMG=" + attackDamage +
                ", Attack Speed=" + (1/attackCooldown) + "/sec");
    }

    /** Handles death of the enemy. */
    @Override
    protected void onDeath() {
        System.out.println("NineTailedFox has been defeated!");
    }

    /** Utility methods for game logic or encounter management. */
    public int getCurrentForm() { return 1; }
    public boolean hasEncounteredPlayer() { return targetPlayer != null; }
    public void resetEncounterState() { System.out.println("NineTailedFox encounter state reset"); }
    public boolean isAttacking() { return isAttacking; }

    /** Dispose textures to prevent memory leaks. */
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
package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.Wall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZhuLong extends Enemy {

    // ------------------- Direction Enum -------------------
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    // ------------------- Animation Fields -------------------
    private Map<Direction, Animation<TextureRegion>> walkAnimations;
    private Direction currentDirection = Direction.DOWN;
    private float stateTime = 0f;           // Timer for animation
    private float animationSpeed = 0.2f;

    // ------------------- Day/Night Logic -------------------
    private boolean eyesOpen = true;        // Eyes affect attack multiplier
    private float dayNightTimer = 0f;       // Timer to toggle eyes open/close
    private Vector2 targetPosition;         // Current target for movement

    // ------------------- Constructor -------------------
    public ZhuLong(float x, float y) {
        super(x, y, 31, 31);

        // Initialize stats
        this.health = 150f;
        this.maxHealth = 300f;
        this.speed = 40f;
        this.attackDamage = 15f;
        this.attackRange = 60f;
        this.detectionRange = 250f;

        initializeAnimations();

        System.out.println("ZhuLong initialized (Directional Animations)");
    }

    // ------------------- Load Animations -------------------
    private void initializeAnimations() {
        walkAnimations = new HashMap<>();

        // Load directional frames
        TextureRegion[] downFrames = loadDirectionFrames("enemies/zhulong/down_", 3);
        if (downFrames[0] != null) walkAnimations.put(Direction.DOWN, new Animation<>(animationSpeed, downFrames));

        TextureRegion[] leftFrames = loadDirectionFrames("enemies/zhulong/left_", 3);
        if (leftFrames[0] != null) walkAnimations.put(Direction.LEFT, new Animation<>(animationSpeed, leftFrames));

        // Mirror left frames if right frames missing
        TextureRegion[] rightFrames = loadDirectionFrames("enemies/zhulong/right_", 3);
        if (rightFrames[0] == null && leftFrames[0] != null) {
            rightFrames = mirrorFrames(leftFrames);
            System.out.println("ZhuLong: Mirrored left frames for right direction.");
        }
        if (rightFrames[0] != null) walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, rightFrames));

        // Load up frames
        TextureRegion[] upFrames = loadDirectionFrames("enemies/zhulong/up_", 2);
        if (upFrames[0] != null) {
            Animation<TextureRegion> upAnim = new Animation<>(animationSpeed, upFrames);
            upAnim.setPlayMode(Animation.PlayMode.LOOP);
            walkAnimations.put(Direction.UP, upAnim);
        } else if (downFrames[0] != null) {
            walkAnimations.put(Direction.UP, new Animation<>(animationSpeed, downFrames));
            System.out.println("ZhuLong: UP texture missing, using DOWN as fallback.");
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
                    System.out.println("Missing texture: " + path);
                }
            } catch (Exception e) {
                frames[i] = null;
            }
        }
        return frames;
    }

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

    // ------------------- Update Logic -------------------
    @Override
    public void update(float delta, List<Wall> walls) {
        if (!isAlive()) return;

        stateTime += delta;

        // Toggle eyes open/close for day/night effect
        updateDayNightCycle(delta);

        // Update current movement direction based on velocity
        updateDirection();

        // Movement logic: speed affected by eyes
        float currentSpeed = eyesOpen ? speed : speed * 0.5f;
        if (targetPosition != null) {
            Vector2 direction = new Vector2(targetPosition.x - position.x, targetPosition.y - position.y);
            if (direction.len() > 1f) {
                direction.nor();
                velocity.set(direction.x * currentSpeed, direction.y * currentSpeed);
            } else {
                velocity.set(0f, 0f);
            }
        }

        // Apply standard enemy movement, collision, etc.
        super.update(delta, walls);
    }

    private void updateDayNightCycle(float delta) {
        dayNightTimer += delta;
        if (dayNightTimer >= 5.0f) {
            eyesOpen = !eyesOpen;
            dayNightTimer = 0f;
            System.out.println("ZhuLong eyes: " + (eyesOpen ? "OPEN" : "CLOSED"));
        }
    }

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

        Animation<TextureRegion> anim = walkAnimations.get(currentDirection);
        TextureRegion currentFrame = (anim != null) ? anim.getKeyFrame(stateTime, true) : null;

        if (currentFrame != null) {
            float drawWidth = 80f;
            float drawHeight = 80f;
            float offsetX = (bounds.width - drawWidth) / 2;
            float offsetY = (bounds.height - drawHeight) / 2;

            batch.setColor(Color.WHITE);
            batch.draw(currentFrame, position.x + offsetX, position.y + offsetY, drawWidth, drawHeight);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    // ------------------- Attack -------------------
    @Override
    public void attack() {
        float damageMultiplier = eyesOpen ? 1.5f : 0.8f; // Eyes open = stronger attack
        System.out.println("ZhuLong attacks! Damage multiplier: " + damageMultiplier);

        if (player != null && isInAttackRange()) {
            player.takeDamage(attackDamage * damageMultiplier);
        }
    }

    private boolean isInAttackRange() {
        if (player == null) return false;
        return position.dst(player.getPosition()) <= attackRange;
    }

    // ------------------- Difficulty Adjustment -------------------
    @Override
    public void adjustDifficulty(int level) {
        this.maxHealth = 200 + (level * 40);
        this.health = maxHealth;
        this.attackDamage = 10 + (level * 3);
        this.speed = 40f + (level * 2f);
    }

    // ------------------- Death -------------------
    @Override
    protected void onDeath() {
        System.out.println("ZhuLong Defeated!");
    }

    // ------------------- Dispose -------------------
    public void dispose() {
        if (walkAnimations != null) {
            for (Animation<TextureRegion> anim : walkAnimations.values()) {
                Object[] frames = anim.getKeyFrames();
                for (Object frameObj : frames) {
                    if (frameObj instanceof TextureRegion) {
                        TextureRegion tr = (TextureRegion) frameObj;
                        if (tr.getTexture() != null) tr.getTexture().dispose();
                    }
                }
            }
        }
    }

    // ------------------- Getters & Setters -------------------
    public void setTargetPosition(Vector2 target) { this.targetPosition = target; }
    public boolean isEyesOpen() { return eyesOpen; }
    public void setEyesOpen(boolean open) { this.eyesOpen = open; }
    public int getCurrentForm() { return 0; }
}
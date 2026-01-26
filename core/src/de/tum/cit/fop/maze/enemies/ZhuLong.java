package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Wall;
import de.tum.cit.fop.maze.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZhuLong extends Enemy {

    // Enum for movement directions
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Map<Direction, Animation<TextureRegion>> walkAnimations; // Animations for each direction
    private Direction currentDirection = Direction.DOWN; // Current movement direction
    private float stateTime = 0f; // Timer for animation
    private float animationSpeed = 0.2f; // Speed of animation

    private boolean eyesOpen = true; // Whether ZhuLong's eyes are open
    private float dayNightTimer = 0f; // Timer for day/night cycle (eye open/close)
    private Vector2 targetPosition; // Current target position to move toward

    // Constructor
    public ZhuLong(float x, float y) {
        super(x, y, 31, 31);

        this.health = 150f;
        this.maxHealth = 300f;
        this.speed = 40f;
        this.attackDamage = 15f;
        this.attackRange = 60f;
        this.detectionRange = 250f;

        initializeAnimations();

        System.out.println("ZhuLong initialized (Directional Animations)");
    }

    // Load all directional animations
    private void initializeAnimations() {
        walkAnimations = new HashMap<>();

        try {
            TextureRegion[] downFrames = loadDirectionFrames("enemies/zhulong/down_", 3);
            if (downFrames[0] != null) {
                walkAnimations.put(Direction.DOWN, new Animation<>(animationSpeed, downFrames));
            }

            TextureRegion[] leftFrames = loadDirectionFrames("enemies/zhulong/left_", 3);
            if (leftFrames[0] != null) {
                walkAnimations.put(Direction.LEFT, new Animation<>(animationSpeed, leftFrames));
            }

            TextureRegion[] rightFrames = loadDirectionFrames("enemies/zhulong/right_", 3);
            if (rightFrames[0] == null && leftFrames[0] != null) {
                rightFrames = mirrorFrames(leftFrames); // Mirror left frames for right
                System.out.println("ZhuLong: Mirrored left frames for right direction.");
            }
            if (rightFrames[0] != null) {
                walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, rightFrames));
            }

            TextureRegion[] upFrames = loadDirectionFrames("enemies/zhulong/up_", 2);

            if (upFrames[0] != null) {
                Animation<TextureRegion> upAnim = new Animation<>(animationSpeed, upFrames);
                upAnim.setPlayMode(Animation.PlayMode.LOOP);
                walkAnimations.put(Direction.UP, upAnim);

                System.out.println("ZhuLong: Loaded 2 frames for UP direction.");
            } else {
                System.out.println("ZhuLong: UP texture missing, using DOWN as fallback.");
                if (downFrames[0] != null) {
                    walkAnimations.put(Direction.UP, new Animation<>(animationSpeed, downFrames));
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading ZhuLong animations: " + e.getMessage());
        }
    }

    // Load frames from files for a given direction
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

    // Mirror frames horizontally (for right direction)
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

    // Update ZhuLong logic each frame
    @Override
    public void update(float delta, List<Wall> walls) {
        if (!isAlive()) return;

        stateTime += delta; // Update animation timer

        updateDayNightCycle(delta); // Toggle eyes open/close

        updateDirection(); // Update facing direction

        float currentSpeed = eyesOpen ? speed : speed * 0.5f; // Half speed if eyes closed

        if (targetPosition != null) {
            Vector2 direction = new Vector2(
                    targetPosition.x - position.x,
                    targetPosition.y - position.y
            );
            if (direction.len() > 1f) {
                direction.nor();
                velocity.set(direction.x * currentSpeed, direction.y * currentSpeed);
            } else {
                velocity.set(0, 0);
            }
        }

        super.update(delta, walls); // Call parent update for movement/collision
    }

    // Toggle eyes open/close every 5 seconds
    private void updateDayNightCycle(float delta) {
        dayNightTimer += delta;
        if (dayNightTimer >= 5.0f) {
            eyesOpen = !eyesOpen;
            dayNightTimer = 0f;
            System.out.println("ZhuLong eyes: " + (eyesOpen ? "OPEN" : "CLOSED"));
        }
    }

    // Update movement direction based on velocity
    private void updateDirection() {
        if (velocity.len() > 0.1f) {
            float angle = (float) Math.atan2(velocity.y, velocity.x) * 180f / (float) Math.PI;
            if (Math.abs(angle) <= 45f) {
                currentDirection = Direction.RIGHT;
            } else if (angle > 45f && angle <= 135f) {
                currentDirection = Direction.UP;
            } else if (angle < -45f && angle >= -135f) {
                currentDirection = Direction.DOWN;
            } else {
                currentDirection = Direction.LEFT;
            }
        }
    }

    // Render ZhuLong on screen
    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive()) return;

        Animation<TextureRegion> anim = walkAnimations.get(currentDirection);
        TextureRegion currentFrame = null;

        if (anim != null) {
            currentFrame = anim.getKeyFrame(stateTime, true); // Get current animation frame
        }

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

    // Attack method
    @Override
    public void attack() {
        float damageMultiplier = eyesOpen ? 1.5f : 0.8f; // Stronger if eyes open
        System.out.println("ZhuLong attacks! Damage multiplier: " + damageMultiplier);
        if (targetPlayer != null && isInAttackRange()) {
            targetPlayer.takeDamage(attackDamage * damageMultiplier); // Apply damage
        }
    }

    // Adjust stats according to difficulty
    @Override
    public void adjustDifficulty(int level) {
        this.maxHealth = 200 + (level * 40);
        this.health = this.maxHealth;
        this.attackDamage = 10 + (level * 3);
        this.speed = 40f + (level * 2f);
    }

    // Called when ZhuLong dies
    @Override
    protected void onDeath() {
        System.out.println("ZhuLong Defeated!");
    }

    // Dispose textures to free memory
    public void dispose() {
        if (walkAnimations != null) {
            for (Animation<TextureRegion> anim : walkAnimations.values()) {
                Object[] frames = anim.getKeyFrames();
                for (Object frameObj : frames) {
                    if (frameObj instanceof TextureRegion) {
                        TextureRegion tr = (TextureRegion) frameObj;
                        if (tr.getTexture() != null) {
                            // Texture dispose can be added if needed
                        }
                    }
                }
            }
        }
    }

    // Set target position to move toward
    public void setTargetPosition(Vector2 target) { this.targetPosition = target; }

    // Check if eyes are open
    public boolean isEyesOpen() { return eyesOpen; }

    // Manually set eyes open/close
    public void setEyesOpen(boolean open) { this.eyesOpen = open; }

    // Current form (for bosses with multiple phases)
    public int getCurrentForm() { return 0; }
}
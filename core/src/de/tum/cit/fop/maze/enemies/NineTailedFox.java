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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NineTailedFox extends Enemy {

    // Enum representing movement directions
    private enum Direction { UP, DOWN, LEFT, RIGHT }

    // Animations for walking in each direction
    private Map<Direction, Animation<TextureRegion>> walkAnimations;
    // Static frames when idle
    private Map<Direction, TextureRegion> idleFrames;

    private Direction currentDirection = Direction.DOWN; // Current facing direction
    private float stateTime = 0f; // Timer for animation frames
    private float animationSpeed = 0.15f;

    private boolean isAttacking = false; // Tracks whether currently attacking
    private float attackCooldown = 1.0f; // Time between attacks
    private float attackTimer = 0f; // Countdown until next attack allowed

    public NineTailedFox(float x, float y) {
        super(x, y, 30, 30);

        // Initialize stats
        this.speed = 50f;
        this.maxHealth = 40f;
        this.health = maxHealth;
        this.attackDamage = 6f;
        this.attackRange = 40f;
        this.detectionRange = 200f;

        initAnimations(); // Load sprite animations

        System.out.println("NineTailedFox initialized with melee attack");
    }

    // Load animations for all directions
    private void initAnimations() {
        walkAnimations = new HashMap<>();
        idleFrames = new HashMap<>();

        try {
            loadDirectionAnimation("down", Direction.DOWN);
            loadDirectionAnimation("up", Direction.UP);
            loadDirectionAnimation("left", Direction.LEFT);
            loadDirectionAnimation("right", Direction.RIGHT);

            System.out.println("Animations loaded successfully");
        } catch (Exception e) {
            System.out.println("Error loading animations: " + e.getMessage());
            loadFallbackTexture(); // Load default texture if specific frames fail
        }
    }

    // Load animation frames for a single direction
    private void loadDirectionAnimation(String baseName, Direction dir) {
        TextureRegion[] frames = new TextureRegion[3]; // assume 3 frames per direction

        for (int i = 0; i < 3; i++) {
            String path = "enemies/nine_tailed_fox/" + baseName + "_" + (i + 1) + ".png";
            try {
                if (Gdx.files.internal(path).exists()) {
                    Texture tex = new Texture(Gdx.files.internal(path));
                    frames[i] = new TextureRegion(tex);
                }
            } catch (Exception ignored) {}
        }

        if (frames[0] != null) {
            walkAnimations.put(dir, new Animation<>(animationSpeed, frames));
            idleFrames.put(dir, frames[1]); // Middle frame used as idle
        }
    }

    // Fallback texture in case specific direction frames are missing
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

    @Override
    public void update(float delta, List<Wall> walls) {
        super.update(delta, walls);
        stateTime += delta; // Advance animation timer

        // Decrease attack cooldown timer
        if (attackTimer > 0f) attackTimer -= delta;

        // Update facing direction based on current movement
        updateDirection();

        // Check for player proximity to trigger melee attack
        if (player != null) {
            float distance = position.dst(player.getPosition());
            if (distance <= attackRange && attackTimer <= 0f) {
                performMeleeAttack(player);
                isAttacking = true;
                attackTimer = attackCooldown; // Reset attack cooldown
            }
        }
    }

    // Update the current facing direction based on velocity
    private void updateDirection() {
        if (velocity.len() > 0.1f) { // only update if moving
            float angle = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.x));
            if (angle >= -45 && angle <= 45) currentDirection = Direction.RIGHT;
            else if (angle > 45 && angle <= 135) currentDirection = Direction.UP;
            else if (angle < -45 && angle >= -135) currentDirection = Direction.DOWN;
            else currentDirection = Direction.LEFT;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive()) return;

        boolean moving = velocity.len() > 0.1f; // Determine if moving
        TextureRegion frame = idleFrames.get(currentDirection); // Default to idle

        // If moving, select walking animation frame
        if (moving && walkAnimations.containsKey(currentDirection)) {
            frame = walkAnimations.get(currentDirection).getKeyFrame(stateTime, true);
        }

        if (frame != null) {
            // Draw frame centered in bounds
            float drawWidth = 50f;
            float drawHeight = 50f;
            float offsetX = (bounds.width - drawWidth) / 2f;
            float offsetY = (bounds.height - drawHeight) / 2f;
            batch.draw(frame, position.x + offsetX, position.y + offsetY, drawWidth, drawHeight);
        }
    }

    @Override
    public void attack() {
        if (player != null) performMeleeAttack(player); // Trigger melee attack
    }

    // Performs melee attack on player if in range
    private void performMeleeAttack(Player target) {
        if (target == null) return;

        float distance = position.dst(target.getPosition());
        if (distance > attackRange) return; // only attack if within range

        float damage = attackDamage;

        // 10% chance to do critical hit
        if (Math.random() < 0.1f) damage *= 1.5f;

        target.takeDamage(damage);

        System.out.println("[MELEE] NineTailedFox attacks player for " + damage + " damage");
    }

    @Override
    public void adjustDifficulty(int level) {
        // Adjust stats based on difficulty level
        this.maxHealth = 30 + level * 8;
        this.health = maxHealth;
        this.attackDamage = 8 + level * 2;
        this.speed = 90f + level * 5f;
        this.attackCooldown = Math.max(0.6f, 0.8f - level * 0.05f);

        System.out.println(this.getClass().getSimpleName() +
                " adjusted - HP=" + maxHealth +
                ", MELEE DMG=" + attackDamage +
                ", Attack Speed=" + (1/attackCooldown) + "/sec");
    }
    @Override
    protected void onDeath() {
        System.out.println("NineTailedFox has been defeated!");
    }

    // Utility getters for external checks
    public int getCurrentForm() { return 1; }
    public boolean hasEncounteredPlayer() { return player != null; }
    public void resetEncounterState() { System.out.println("NineTailedFox encounter state reset"); }
    public boolean isAttacking() { return isAttacking; }

    // Dispose all textures to avoid memory leaks
    public void dispose() {
        for (Animation<TextureRegion> anim : walkAnimations.values()) {
            for (TextureRegion frame : anim.getKeyFrames()) {
                if (frame != null && frame.getTexture() != null) frame.getTexture().dispose();
            }
        }
        for (TextureRegion frame : idleFrames.values()) {
            if (frame != null && frame.getTexture() != null) frame.getTexture().dispose();
        }
    }
}
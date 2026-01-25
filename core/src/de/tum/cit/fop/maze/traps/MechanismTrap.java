package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.Player;

/**
 * MechanismTrap is a basic trap that deals damage to the player.
 * It has an activation animation that flashes between active/inactive states.
 */
public class MechanismTrap extends Trap {

    private Texture inactiveTexture;   // Texture when trap is idle
    private Texture activeTexture;     // Texture when trap is active
    private boolean isAnimating;       // Is the trap currently animating activation
    private float animTimer;           // Timer for animation
    private float animDuration = 0.3f; // Total animation duration
    private static final float TRAP_SIZE = 32f; // Trap size in pixels

    // ------------------- Constructor -------------------
    public MechanismTrap(float x, float y) {
        super(x, y, TRAP_SIZE, TRAP_SIZE);

        // Trap cooldown and activation delay
        this.cooldown = 4f;
        this.activationDelay = 0.3f;

        loadTextures();
    }

    // ------------------- Load Textures -------------------
    private void loadTextures() {
        try {
            inactiveTexture = new Texture(Gdx.files.internal("traps/mti.png"));
            activeTexture = new Texture(Gdx.files.internal("traps/mta.png"));
            System.out.println("MechanismTrap textures loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading MechanismTrap textures: " + e.getMessage());
        }
    }

    // ------------------- Render -------------------
    @Override
    public void render(SpriteBatch batch) {
        float drawX = bounds.x;
        float drawY = bounds.y;
        float drawWidth = bounds.width;
        float drawHeight = bounds.height;

        // Flashing animation when animating
        if (isAnimating) {
            boolean showActive = ((int)(animTimer / 0.1f)) % 2 == 0;

            if (showActive && activeTexture != null) {
                batch.draw(activeTexture, drawX, drawY, drawWidth, drawHeight);
            } else if (inactiveTexture != null) {
                batch.draw(inactiveTexture, drawX, drawY, drawWidth, drawHeight);
            }

        } else if (activated && activeTexture != null) {
            // Show active texture if permanently activated
            batch.draw(activeTexture, drawX, drawY, drawWidth, drawHeight);

        } else if (inactiveTexture != null) {
            // Default idle texture
            batch.draw(inactiveTexture, drawX, drawY, drawWidth, drawHeight);
        }
    }

    // ------------------- Activation -------------------
    @Override
    public void activate(Player player) {
        // Damage scales with player level
        int currentLevel = (int) player.getStats().getExpSystem().getCurrentLevel();
        float trapDamage = 10 + (currentLevel * 5);

        player.takeDamage(trapDamage);

        // Start animation
        isAnimating = true;
        animTimer = 0;

        System.out.println("MechanismTrap Level " + currentLevel + " activated! Damage: " + trapDamage);
    }

    // ------------------- Update -------------------
    @Override
    public void update(float delta) {
        super.update(delta);

        // Update animation timer
        if (isAnimating) {
            animTimer += delta;
            if (animTimer >= animDuration) {
                isAnimating = false;
                animTimer = 0;
            }
        }
    }

    // ------------------- Reset -------------------
    @Override
    public void reset() {
        super.reset();
        activated = false;
        isAnimating = false;
        animTimer = 0;
    }

    // ------------------- Texture Check -------------------
    @Override
    public boolean hasTexture() {
        return true;
    }

    // ------------------- Dispose -------------------
    public void dispose() {
        if (inactiveTexture != null) inactiveTexture.dispose();
        if (activeTexture != null) activeTexture.dispose();
    }
}
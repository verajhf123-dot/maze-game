package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;

/**
 * Base class for all traps in the game.
 * Handles positioning, activation cooldowns, and collision detection with the player.
 */
public abstract class Trap {

    protected Rectangle bounds;      // The trap's hitbox / position
    protected boolean activated;      // Is the trap currently activated
    protected boolean visible;        // Should the trap be rendered
    protected float activationDelay;  // Delay before the trap activates after triggering
    protected float cooldown;         // Time before the trap can be triggered again
    protected float currentCooldown;  // Tracks the cooldown timer

    // ------------------- Constructor -------------------
    public Trap(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.activated = false;
        this.visible = true;
        this.currentCooldown = 0;
        this.activationDelay = 0;
        this.cooldown = 0;
    }

    // ------------------- Update -------------------
    /**
     * Updates the trap state, reducing cooldowns and resetting after activation.
     * @param delta Time elapsed since last frame
     */
    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;

            // Reset trap if cooldown finished
            if (currentCooldown <= 0 && activated) {
                reset();
            }
        }
    }

    // ------------------- Render -------------------
    /**
     * Render the trap. Implementation depends on trap type.
     * @param batch SpriteBatch used for rendering
     */
    public abstract void render(SpriteBatch batch);

    // ------------------- Activation -------------------
    /**
     * Trigger the trap on a player.
     * @param player The player being affected
     */
    public abstract void activate(Player player);

    /**
     * Resets the trap to its idle state.
     */
    public void reset() {
        activated = false;
        currentCooldown = 0;
    }

    // ------------------- Collision Check -------------------
    /**
     * Checks if the player overlaps the trap and triggers it if possible.
     * @param player The player to check collision against
     */
    public void checkActivation(Player player) {
        if (currentCooldown <= 0 && bounds.overlaps(player.getHitbox())) {
            activate(player);
            activated = true;
            currentCooldown = cooldown;
        }
    }

    // ------------------- Utility -------------------
    /**
     * Override if the trap has a texture to render
     * @return true if a texture exists
     */
    public boolean hasTexture() {
        return true;
    }

    // ------------------- Getters -------------------
    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isVisible() {
        return visible;
    }
}
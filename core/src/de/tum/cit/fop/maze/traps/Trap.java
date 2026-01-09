package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;

public abstract class Trap {
    protected Rectangle bounds;
    protected boolean activated;
    protected boolean visible;
    protected float activationDelay;
    protected float cooldown;
    protected float currentCooldown;

    public Trap(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
        this.activated = false;
        this.visible = true;
        this.currentCooldown = 0;
    }

    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            if (currentCooldown <= 0 && activated) {
                reset();
            }
        }
    }

    public abstract void render(SpriteBatch batch);
    public abstract void activate(Player player);
    public abstract void reset();

    public void checkActivation(Player player) {
        if (currentCooldown <= 0 && bounds.overlaps(player.getHitbox())) {
            activate(player);
            activated = true;
            currentCooldown = cooldown;
        }
    }
    public boolean hasTexture() {
        return true;
    }

    public Rectangle getBounds() { return bounds; }
    public boolean isActivated() { return activated; }
    public boolean isVisible() { return visible; }
}
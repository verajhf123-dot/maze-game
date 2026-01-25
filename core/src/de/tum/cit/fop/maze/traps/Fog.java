package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Circle;
import de.tum.cit.fop.maze.Player;

/**
 * Fog trap reduces player visibility within a radius.
 * Can optionally display a particle effect or fallback texture.
 */
public class Fog extends Trap {

    private Texture fogTexture;           // Fog icon texture
    private ParticleEffect fogEffect;     // Particle effect for active fog
    private Circle effectArea;            // Circular area of effect
    private boolean effectActive;         // Is fog currently active
    private float effectDuration = 5f;    // Duration fog remains active
    private float currentEffectTime = 0f; // Timer for active effect

    private static Texture fallbackTexture; // Simple fallback texture if assets fail
    private static final float FOG_ICON_SIZE = 32f;

    // ------------------- Constructor -------------------
    public Fog(float x, float y) {
        super(x, y, FOG_ICON_SIZE, FOG_ICON_SIZE);

        // Center the circular effect area on trap position
        float centerX = x + FOG_ICON_SIZE / 2;
        float centerY = y + FOG_ICON_SIZE / 2;
        this.effectArea = new Circle(centerX, centerY, 200);

        this.effectActive = false;
        this.cooldown = 10f;

        loadAssets();
    }

    // ------------------- Asset Loading -------------------
    private void loadAssets() {
        // Load fog icon texture
        if (Gdx.files.internal("traps/fog.png").exists()) {
            try {
                fogTexture = new Texture(Gdx.files.internal("traps/fog.png"));
            } catch (Exception e) {
                System.out.println("Failed to load fog.png");
            }
        }

        // Load particle effect if exists
        if (Gdx.files.internal("particles/fog.p").exists()) {
            try {
                fogEffect = new ParticleEffect();
                fogEffect.load(Gdx.files.internal("particles/fog.p"), Gdx.files.internal("particles"));
                fogEffect.getEmitters().first().setPosition(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
            } catch (Exception e) {
                System.out.println("Failed to load fog particles. Effect disabled.");
                fogEffect = null;
            }
        } else {
            fogEffect = null;
        }

        // Create fallback texture if not already created
        if (fallbackTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            fallbackTexture = new Texture(p);
            p.dispose();
        }
    }

    // ------------------- Update -------------------
    @Override
    public void update(float delta) {
        super.update(delta);

        if (effectActive) {
            currentEffectTime += delta;

            // Update particle effect if present
            if (fogEffect != null) {
                fogEffect.update(delta);
            }

            // Deactivate effect if duration exceeded
            if (currentEffectTime >= effectDuration) {
                deactivateEffect();
            }
        }
    }

    // ------------------- Render -------------------
    @Override
    public void render(SpriteBatch batch) {
        // Draw inactive fog as icon
        if (!effectActive && visible) {
            if (fogTexture != null) {
                batch.draw(fogTexture, bounds.x, bounds.y, bounds.width, bounds.height);
            } else {
                batch.setColor(Color.GRAY);
                batch.draw(fallbackTexture, bounds.x, bounds.y, bounds.width, bounds.height);
                batch.setColor(Color.WHITE);
            }
        }

        // Draw active fog effect
        if (effectActive) {
            if (fogEffect != null) {
                fogEffect.draw(batch);
            } else {
                batch.setColor(0.8f, 0.8f, 0.8f, 0.5f);
                float size = effectArea.radius * 2;
                batch.draw(fallbackTexture, effectArea.x - effectArea.radius, effectArea.y - effectArea.radius, size, size);
                batch.setColor(Color.WHITE);
            }
        }
    }

    // ------------------- Activation -------------------
    @Override
    public void activate(Player player) {
        if (!effectActive && currentCooldown <= 0) {
            effectActive = true;
            currentEffectTime = 0f;
            currentCooldown = cooldown;
            activated = true;

            if (fogEffect != null) {
                fogEffect.getEmitters().first().setPosition(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
                fogEffect.start();
            }

            System.out.println("Fog Activated!");
        }
    }

    // Deactivate fog effect
    private void deactivateEffect() {
        effectActive = false;
        if (fogEffect != null) {
            fogEffect.allowCompletion();
        }
    }

    // ------------------- Reset -------------------
    @Override
    public void reset() {
        effectActive = false;
        activated = false;
        currentEffectTime = 0f;
    }

    // ------------------- Player Interaction -------------------
    /**
     * Check if player is inside the fog area
     */
    public boolean isPlayerInFog(Player player) {
        if (!effectActive) return false;

        Circle playerCircle = new Circle(
                player.getPosition().x + player.getHitbox().width / 2,
                player.getPosition().y + player.getHitbox().height / 2,
                player.getHitbox().width / 2
        );

        return effectArea.overlaps(playerCircle);
    }

    /**
     * Returns visibility multiplier (0.0 = fully obscured, 1.0 = normal)
     */
    public float getVisibilityReduction() {
        return effectActive ? 0.4f : 1.0f;
    }

    // ------------------- Dispose -------------------
    public void dispose() {
        if (fogTexture != null) fogTexture.dispose();
        if (fogEffect != null) fogEffect.dispose();
    }
}
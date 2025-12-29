package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Circle;
import de.tum.cit.fop.maze.Player;

public class Fog extends Trap {
    private Texture fogTexture;
    private ParticleEffect fogEffect;
    private Circle effectArea;
    private boolean effectActive;
    private float effectDuration = 5f;
    private float currentEffectTime;
    private static Texture fallbackTexture;

    public Fog(float x, float y) {
        super(x, y, 16, 16);
        this.effectArea = new Circle(x + 32, y + 32, 200);
        this.effectActive = false;
        this.cooldown = 10f;

        loadAssets();
    }

    private void loadAssets() {
        try {
            fogTexture = new Texture(Gdx.files.internal("traps/fog.png"));
            fogEffect = new ParticleEffect();
            fogEffect.load(Gdx.files.internal("particles/fog.p"), Gdx.files.internal("particles"));
            fogEffect.getEmitters().first().setPosition(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        } catch (Exception e) {
            System.out.println("Fog assets missing. Using Transparent Box.");
            fogTexture = null;
            fogEffect = null;
        }
        if (fallbackTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            fallbackTexture = new Texture(p);
            p.dispose();
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (effectActive) {
            currentEffectTime += delta;
            fogEffect.update(delta);

            if (currentEffectTime >= effectDuration) {
                deactivateEffect();
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (visible && fogTexture != null) {
            batch.draw(fogTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        } else if (visible) {
            batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
            batch.draw(fallbackTexture, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
        }

        if (effectActive) {
            if (fogEffect != null) {
                fogEffect.draw(batch, Gdx.graphics.getDeltaTime());
            } else {
                batch.setColor(0.8f, 0.8f, 0.8f, 0.5f);
                float size = effectArea.radius * 2;
                batch.draw(fallbackTexture, effectArea.x - effectArea.radius, effectArea.y - effectArea.radius, size, size);
                batch.setColor(Color.WHITE);
            }
        }
    }

    @Override
    public void activate(Player player) {
        if (!effectActive && currentCooldown <= 0) {
            effectActive = true;
            currentEffectTime = 0;
            currentCooldown = cooldown;
            activated = true;

            fogEffect.getEmitters().first().setPosition(bounds.x + bounds.width/2, bounds.y + bounds.height/2);
            fogEffect.start();
        }
    }

    private void deactivateEffect() {
        effectActive = false;
        fogEffect.allowCompletion();
    }

    @Override
    public void reset() {
        effectActive = false;
        activated = false;
        currentEffectTime = 0;
    }

    public boolean isPlayerInFog(Player player) {
        if (!effectActive) return false;

        Circle playerCircle = new Circle(
                player.getPosition().x + player.getHitbox().width/2,
                player.getPosition().y + player.getHitbox().height/2,
                player.getHitbox().width/2
        );

        return effectArea.overlaps(playerCircle);
    }

    public float getVisibilityReduction() {
        return effectActive ? 0.4f : 1.0f;
    }
    public void dispose() {
        if (fogTexture != null) {
            fogTexture.dispose();
        }
        if (fogEffect != null) {
            fogEffect.dispose();
        }
    }
}

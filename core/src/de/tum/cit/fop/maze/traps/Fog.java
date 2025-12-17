package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.Gdx;
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

    public Fog(float x, float y) {
        super(x, y, 64, 64);
        this.effectArea = new Circle(x + 32, y + 32, 200);
        this.effectActive = false;
        this.cooldown = 10f;

        loadAssets();
    }

    private void loadAssets() {
        fogTexture = new Texture(Gdx.files.internal("traps/fog.png"));
        fogEffect = new ParticleEffect();
        fogEffect.load(Gdx.files.internal("particles/fog.p"), Gdx.files.internal("particles"));
        fogEffect.getEmitters().first().setPosition(bounds.x + bounds.width/2, bounds.y + bounds.height/2);
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
        if (visible) {
            batch.draw(fogTexture, bounds.x, bounds.y, bounds.width, bounds.height);
        }

        if (effectActive) {
            fogEffect.draw(batch, Gdx.graphics.getDeltaTime());
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

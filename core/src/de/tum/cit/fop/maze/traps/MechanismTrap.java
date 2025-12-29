package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.Player;

public class MechanismTrap extends Trap {
    private Texture inactiveTexture;
    private Texture activeTexture;
    private Animation<TextureRegion> activationAnim;
    private float animStateTime;
    private boolean isAnimating;
    private float damage = 30f;
    private boolean hasTexture = false;

    public MechanismTrap(float x, float y) {
        super(x, y, 32, 32);
        this.cooldown = 4f;
        this.activationDelay = 0.3f;

        loadTextures();
    }

    private void loadTextures() {
        if (Gdx.files.internal("traps/mechanism_inactive.png").exists()) {
            inactiveTexture = new Texture(Gdx.files.internal("traps/mechanism_inactive.png"));
            hasTexture = true;
        }

        if (Gdx.files.internal("traps/mechanism_active.png").exists()) {
            activeTexture = new Texture(Gdx.files.internal("traps/mechanism_active.png"));
        }

        if (Gdx.files.internal("traps/mechanism_anim.png").exists()) {
            Texture animSheet = new Texture(Gdx.files.internal("traps/mechanism_anim.png"));
            TextureRegion[][] frames = TextureRegion.split(animSheet, 32, 32);
            TextureRegion[] animFrames = new TextureRegion[3];
            System.arraycopy(frames[0], 0, animFrames, 0, 3);
            activationAnim = new Animation<>(0.1f, animFrames);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (inactiveTexture != null || activeTexture != null) {
            // 有贴图
            if (isAnimating && activationAnim != null) {
                batch.draw(
                        activationAnim.getKeyFrame(animStateTime, false),
                        bounds.x, bounds.y
                );
            } else if (activated && activeTexture != null) {
                batch.draw(activeTexture, bounds.x, bounds.y);
            } else if (inactiveTexture != null) {
                batch.draw(inactiveTexture, bounds.x, bounds.y);
            }
        }
        // ❗️没贴图的情况不能在这里画方块
    }

    @Override
    public void activate(Player player) {
        if (!activated) {
            isAnimating = true;
            animStateTime = 0;

            // 延迟触发伤害
            new Thread(() -> {
                try {
                    Thread.sleep((long)(activationDelay * 1000));
                    if (bounds.overlaps(player.getHitbox())) {
                        player.takeDamage(damage);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void reset() {
        activated = false;
        isAnimating = false;
        animStateTime = 0;
    }
    public void dispose() {
        if (inactiveTexture != null) {
            inactiveTexture.dispose();
        }
        if (activeTexture != null) {
            activeTexture.dispose();
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isAnimating) {
            animStateTime += delta;
            if (activationAnim.isAnimationFinished(animStateTime)) {
                isAnimating = false;
            }
        }
    }
}

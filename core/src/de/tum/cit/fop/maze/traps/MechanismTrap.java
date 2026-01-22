package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.Player;

public class MechanismTrap extends Trap {
    private Texture inactiveTexture;
    private Texture activeTexture;
    private boolean isAnimating;
    private float animTimer;
    private float animDuration = 0.3f;
    private static final float TRAP_SIZE = 32f;
    public MechanismTrap(float x, float y) {
        super(x, y, TRAP_SIZE, TRAP_SIZE);
        this.cooldown = 4f;
        this.activationDelay = 0.3f;
        loadTextures();
    }

    private void loadTextures() {
        try {
            inactiveTexture = new Texture(Gdx.files.internal("traps/mti.png"));
            activeTexture = new Texture(Gdx.files.internal("traps/mta.png"));
            System.out.println("MechanismTrap textures loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading MechanismTrap textures: " + e.getMessage());
        }
    }


    @Override
    public void render(SpriteBatch batch) {
        float drawX = bounds.x;
        float drawY = bounds.y;
        float drawWidth = bounds.width;
        float drawHeight = bounds.height;

        if (isAnimating) {
            boolean showActive = ((int)(animTimer / 0.1f)) % 2 == 0;
            if (showActive && activeTexture != null) {
                batch.draw(activeTexture, drawX, drawY, drawWidth, drawHeight);
            } else if (inactiveTexture != null) {
                batch.draw(inactiveTexture, drawX, drawY, drawWidth, drawHeight);
            }
        } else if (activated && activeTexture != null) {
            batch.draw(activeTexture, drawX, drawY, drawWidth, drawHeight);
        } else if (inactiveTexture != null) {
            batch.draw(inactiveTexture, drawX, drawY, drawWidth, drawHeight);
        }
    }

    @Override
    public void activate(Player player) {
        int currentLevel = (int) player.getStats().getExpSystem().getCurrentLevel();
        float trapDamage = 10 + (currentLevel * 5);

        player.takeDamage(trapDamage);
        isAnimating = true;
        animTimer = 0;

        System.out.println("MechanismTrap Level " + currentLevel + " activated! Damage: " + trapDamage);
    }

    @Override
    public void reset() {
        super.reset();
        activated = false;
        isAnimating = false;
        animTimer = 0;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isAnimating) {
            animTimer += delta;
            if (animTimer >= animDuration) {
                isAnimating = false;
                animTimer = 0;
            }
        }
    }

    @Override
    public boolean hasTexture() {
        return true;
    }

    public void dispose() {
        if (inactiveTexture != null) inactiveTexture.dispose();
        if (activeTexture != null) activeTexture.dispose();
    }
}

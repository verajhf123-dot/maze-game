package de.tum.cit.fop.maze.traps;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
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
    private static Texture fallbackTexture;
    private boolean hasTexture = false;

    public MechanismTrap(float x, float y) {
        super(x, y, 16, 16);
        this.cooldown = 4f;
        this.activationDelay = 0.3f;

        loadTextures();
    }

    private void loadTextures() {
        try{
        inactiveTexture = new Texture(Gdx.files.internal("traps/mechanism_inactive.png"));
        activeTexture = new Texture(Gdx.files.internal("traps/mechanism_active.png"));
        if (Gdx.files.internal("traps/mechanism_inactive.png").exists()) {
            inactiveTexture = new Texture(Gdx.files.internal("traps/mechanism_inactive.png"));
            hasTexture = true;
        }

        if (Gdx.files.internal("traps/mechanism_active.png").exists()) {
            activeTexture = new Texture(Gdx.files.internal("traps/mechanism_active.png"));
        }

        // 创建激活动画
        Texture animSheet = new Texture(Gdx.files.internal("traps/mechanism_anim.png"));
        TextureRegion[][] frames = TextureRegion.split(animSheet, 32, 32);
        TextureRegion[] animFrames = new TextureRegion[3];
        System.arraycopy(frames[0], 0, animFrames, 0, 3);
        activationAnim = new Animation<>(0.1f, animFrames);
    }catch(Exception e){
            System.out.println("MechanismTrap textures missing, using fallback box.");
        }
        if(fallbackTexture ==null){
            Pixmap p =new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(Color.WHITE);
            p.fill();
            fallbackTexture = new Texture(p);
            p.dispose();
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
        if(inactiveTexture != null){
            if (isAnimating) {
                TextureRegion frame = activationAnim.getKeyFrame(animStateTime, false);
                batch.draw(frame, bounds.x, bounds.y);
            } else if (activated) {
                batch.draw(activeTexture, bounds.x, bounds.y);
            } else {
                batch.draw(inactiveTexture, bounds.x, bounds.y);
            }
        }else {
            batch.setColor(activated ? Color.ORANGE : Color.GRAY);
            batch.draw(fallbackTexture, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
        }

        }


        // ❗️没贴图的情况不能在这里画方块
    }

    @Override
    public void activate(Player player) {
        if (!activated) {
            isAnimating = true;
            animStateTime = 0;
            activated = true;

            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    if (player != null && bounds.overlaps(player.getHitbox())) {
                        player.takeDamage(damage);
                        System.out.println("陷阱触发！玩家受到伤害: " + damage);
                    }
                }
            }, activationDelay);
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
            if (activationAnim!= null &&activationAnim.isAnimationFinished(animStateTime)) {
                isAnimating = false;
            }
            if(activationAnim==null){
                isAnimating = false;
            }
        }
    }
}

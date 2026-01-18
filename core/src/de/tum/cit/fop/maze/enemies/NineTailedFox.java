package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;
import java.util.HashMap;
import java.util.Map;

public class NineTailedFox extends Enemy {
    // 动画相关
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Map<Direction, Animation<TextureRegion>> walkAnimations;
    private Map<Direction, TextureRegion> idleFrames;

    private Direction currentDirection = Direction.DOWN;
    private float stateTime = 0f;
    private float animationSpeed = 0.15f;

    // 攻击相关（仅状态，无动画）
    private boolean isAttacking = false;
    private float attackCooldown = 1.0f;
    private float currentAttackCooldown = 0f;

    public NineTailedFox(float x, float y) {
        super(x, y, 48, 48);

        // 敌人数值
        this.speed = 80f;
        this.maxHealth = 40f;
        this.health = maxHealth;
        this.attackDamage = 6f;
        this.attackRange = 40f;
        this.detectionRange = 200f;
        this.attackCooldown = 0.8f;

        // 初始化动画系统
        initializeAnimations();

        System.out.println("NineTailedFox initialized with melee attack");
    }

    private void initializeAnimations() {
        walkAnimations = new HashMap<>();
        idleFrames = new HashMap<>();

        try {
            // 加载每个方向的行走动画（3帧）
            TextureRegion[] downFrames = loadDirectionFrames("enemies/nine_tailed_fox/down_", 3);
            if (downFrames[0] != null) {
                walkAnimations.put(Direction.DOWN, new Animation<>(animationSpeed, downFrames));
                idleFrames.put(Direction.DOWN, downFrames[1]); // 使用第二帧作为待机
            }

            TextureRegion[] upFrames = loadDirectionFrames("enemies/nine_tailed_fox/up_", 3);
            if (upFrames[0] != null) {
                walkAnimations.put(Direction.UP, new Animation<>(animationSpeed, upFrames));
                idleFrames.put(Direction.UP, upFrames[1]);
            }

            TextureRegion[] leftFrames = loadDirectionFrames("enemies/nine_tailed_fox/left_", 3);
            if (leftFrames[0] != null) {
                walkAnimations.put(Direction.LEFT, new Animation<>(animationSpeed, leftFrames));
                idleFrames.put(Direction.LEFT, leftFrames[1]);
            }

            TextureRegion[] rightFrames = loadDirectionFrames("enemies/nine_tailed_fox/right_", 3);
            if (rightFrames[0] != null) {
                walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, rightFrames));
                idleFrames.put(Direction.RIGHT, rightFrames[1]);
            } else if (leftFrames[0] != null) {
                // 如果没有向右图片，镜像向左图片
                TextureRegion[] mirroredFrames = mirrorFrames(leftFrames);
                walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, mirroredFrames));
                idleFrames.put(Direction.RIGHT, mirroredFrames[1]);
            }

            System.out.println("Animations loaded successfully");

        } catch (Exception e) {
            System.out.println("Error loading NineTailedFox animations: " + e.getMessage());
            loadFallbackTexture();
        }
    }

    private TextureRegion[] loadDirectionFrames(String basePath, int frameCount) {
        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            try {
                String path = basePath + (i + 1) + ".png";
                if (Gdx.files.internal(path).exists()) {
                    Texture texture = new Texture(Gdx.files.internal(path));
                    frames[i] = new TextureRegion(texture);
                } else {
                    frames[i] = null;
                }
            } catch (Exception e) {
                frames[i] = null;
            }
        }

        return frames;
    }

    private TextureRegion[] mirrorFrames(TextureRegion[] originalFrames) {
        TextureRegion[] mirrored = new TextureRegion[originalFrames.length];
        for (int i = 0; i < originalFrames.length; i++) {
            if (originalFrames[i] != null) {
                mirrored[i] = new TextureRegion(originalFrames[i]);
                mirrored[i].flip(true, false);
            }
        }
        return mirrored;
    }

    private void loadFallbackTexture() {
        try {
            Texture fallback = new Texture(Gdx.files.internal("enemies/nine_tailed_fox.png"));
            TextureRegion singleFrame = new TextureRegion(fallback);
            Animation<TextureRegion> singleAnim = new Animation<>(1f, singleFrame);

            walkAnimations.put(Direction.DOWN, singleAnim);
            walkAnimations.put(Direction.UP, singleAnim);
            walkAnimations.put(Direction.LEFT, singleAnim);
            walkAnimations.put(Direction.RIGHT, singleAnim);

            idleFrames.put(Direction.DOWN, singleFrame);
            idleFrames.put(Direction.UP, singleFrame);
            idleFrames.put(Direction.LEFT, singleFrame);
            idleFrames.put(Direction.RIGHT, singleFrame);

            System.out.println("Using fallback texture");
        } catch (Exception e) {
            System.out.println("No fallback texture available");
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // 更新动画计时器
        stateTime += delta;

        // 更新攻击冷却
        if (currentAttackCooldown > 0) {
            currentAttackCooldown -= delta;
        }

        // 更新移动方向
        updateDirection();

        // 更新目标玩家
        if (targetPlayer != null) {
            Vector2 playerPos = targetPlayer.getPosition();
            Vector2 foxPos = getPosition();
            float distance = foxPos.dst(playerPos);

            // 检查是否在攻击范围内且可以攻击
            if (distance <= attackRange && currentAttackCooldown <= 0) {
                // 执行近战攻击
                performMeleeAttack(targetPlayer);
                isAttacking = true;
                currentAttackCooldown = attackCooldown;
            }
        }
    }

    private void updateDirection() {
        if (velocity.len() > 0.1f) {
            // 根据速度确定方向
            float angle = (float) Math.atan2(velocity.y, velocity.x) * 180f / (float) Math.PI;

            if (Math.abs(angle) <= 45f) {
                currentDirection = Direction.RIGHT;
            } else if (angle > 45f && angle <= 135f) {
                currentDirection = Direction.UP;
            } else if (angle < -45f && angle >= -135f) {
                currentDirection = Direction.DOWN;
            } else {
                currentDirection = Direction.LEFT;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!isAlive()) return;

        boolean isMoving = velocity.len() > 0.1f;

        TextureRegion currentFrame = null;

        // 获取当前方向的行走动画和待机帧
        Animation<TextureRegion> walkAnim = walkAnimations.get(currentDirection);
        TextureRegion idleFrame = idleFrames.get(currentDirection);

        if (walkAnim != null && idleFrame != null) {
            if (isMoving) {
                currentFrame = walkAnim.getKeyFrame(stateTime, true);
            } else {
                currentFrame = idleFrame;
            }
        }

        if (currentFrame != null) {
            // 统一绘制大小为 64x64
            float drawWidth = 64f;
            float drawHeight = 64f;

            // 计算偏移使图像中心与碰撞框中心对齐
            float offsetX = (bounds.width - drawWidth) / 2;
            float offsetY = (bounds.height - drawHeight) / 2;

            // 绘制图像
            batch.draw(currentFrame,
                    position.x + offsetX,
                    position.y + offsetY,
                    drawWidth,
                    drawHeight);
        }
    }

    @Override
    public void attack() {
        // 实现抽象方法
        if (targetPlayer != null) {
            performMeleeAttack(targetPlayer);
        }
    }

    private void performMeleeAttack(Player player) {
        if (player != null) {
            float distance = position.dst(player.getPosition());
            if (distance <= attackRange) {
                // 应用九尾狐的攻击伤害
                float finalDamage = attackDamage;

                // 可能的暴击系统
                if (Math.random() < 0.1f) { // 10%暴击率
                    finalDamage *= 1.5f;
                    System.out.println("NINE-TAILED FOX CRITICAL HIT!");
                }

                player.takeDamage(finalDamage);

                System.out.println("[MELEE] NineTailedFox attacks player for " + finalDamage + " damage");

                // 触发攻击行为回调
                if (currentBehavior != null) {
                    currentBehavior.onAttack();
                }
            }
        }
    }

    @Override
    public void adjustDifficulty(int level) {
        // 难度调整系统
        this.maxHealth = 30 + (level * 8);
        this.health = this.maxHealth;
        this.attackDamage = 8 + (level * 2);
        this.speed = 90f + (level * 5f);
        this.attackCooldown = Math.max(0.6f, 0.8f - (level * 0.05f));

        System.out.println(this.getClass().getSimpleName() +
                " adjusted - HP=" + maxHealth +
                ", MELEE DMG=" + attackDamage +
                ", Attack Speed=" + (1/attackCooldown) + "/sec");
    }

    @Override
    protected void onDeath() {
        System.out.println("NineTailedFox has been defeated!");
    }

    // 添加缺少的方法
    public int getCurrentForm() {
        return 1; // 九尾狐只有一个形态
    }

    public boolean hasEncounteredPlayer() {
        return targetPlayer != null;
    }

    public void resetEncounterState() {
        System.out.println("NineTailedFox encounter state reset");
        // 可以在这里重置任何遭遇相关的状态
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    // 移除 @Override 注解
    public void dispose() {
        // 释放所有纹理资源
        for (Animation<TextureRegion> anim : walkAnimations.values()) {
            for (TextureRegion frame : anim.getKeyFrames()) {
                if (frame != null && frame.getTexture() != null) {
                    frame.getTexture().dispose();
                }
            }
        }

        for (TextureRegion idleFrame : idleFrames.values()) {
            if (idleFrame != null && idleFrame.getTexture() != null) {
                idleFrame.getTexture().dispose();
            }
        }
    }
}

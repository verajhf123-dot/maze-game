package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Wall; // 关键：导入 Wall
import de.tum.cit.fop.maze.Player;

import java.util.HashMap;
import java.util.List; // 关键：导入 List
import java.util.Map;

public class ZhuLong extends Enemy {

    // --- 动画相关变量 ---
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Map<Direction, Animation<TextureRegion>> walkAnimations;
    private Direction currentDirection = Direction.DOWN;
    private float stateTime = 0f;
    private float animationSpeed = 0.2f; // 动画播放速度

    // --- 烛龙特有机制 ---
    private boolean eyesOpen = true; // 睁眼状态
    private float dayNightTimer = 0f;
    private Vector2 targetPosition;

    public ZhuLong(float x, float y) {
        // 建议：如果烛龙是 Boss，碰撞箱(32,32)可能比(16,16)更合适，防止穿模，但要确保能过路
        super(x, y, 32, 32);

        // 初始化属性
        this.health = 150f;
        this.maxHealth = 150f;
        this.speed = 40f;
        this.attackDamage = 15f;
        this.attackRange = 60f;
        this.detectionRange = 250f;

        // 初始化动画
        initializeAnimations();

        System.out.println("ZhuLong initialized (Directional Animations)");
    }

    private void initializeAnimations() {
        walkAnimations = new HashMap<>();

        try {
            // 1. 加载向下 (3帧)
            TextureRegion[] downFrames = loadDirectionFrames("enemies/zhulong/down_", 3);
            if (downFrames[0] != null) {
                walkAnimations.put(Direction.DOWN, new Animation<>(animationSpeed, downFrames));
            }

            // 2. 加载向左 (3帧)
            TextureRegion[] leftFrames = loadDirectionFrames("enemies/zhulong/left_", 3);
            if (leftFrames[0] != null) {
                walkAnimations.put(Direction.LEFT, new Animation<>(animationSpeed, leftFrames));
            }

            // 3. 加载向右 (3帧)
            // 尝试加载 right_x.png，如果不存在，则镜像翻转左边的图片
            TextureRegion[] rightFrames = loadDirectionFrames("enemies/zhulong/right_", 3);
            if (rightFrames[0] == null && leftFrames[0] != null) {
                rightFrames = mirrorFrames(leftFrames);
                System.out.println("ZhuLong: Mirrored left frames for right direction.");
            }
            if (rightFrames[0] != null) {
                walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, rightFrames));
            }

            // 4. 加载向上 (只有1帧) - [核心修改]
            TextureRegion[] upFrames = loadDirectionFrames("enemies/zhulong/up_", 2);

            if (upFrames[0] != null) {
                // 创建动画，播放模式设为 LOOP (循环播放: 1->2->1->2...)
                Animation<TextureRegion> upAnim = new Animation<>(animationSpeed, upFrames);
                upAnim.setPlayMode(Animation.PlayMode.LOOP);
                walkAnimations.put(Direction.UP, upAnim);

                System.out.println("ZhuLong: Loaded 2 frames for UP direction.");
            } else {
                // 如果图片没找到的保底逻辑
                System.out.println("ZhuLong: UP texture missing, using DOWN as fallback.");
                if (downFrames[0] != null) {
                    walkAnimations.put(Direction.UP, new Animation<>(animationSpeed, downFrames));
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading ZhuLong animations: " + e.getMessage());
        }
    }

    // 加载图片辅助方法
    private TextureRegion[] loadDirectionFrames(String basePath, int frameCount) {
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            try {
                // 假设图片命名为 down_1.png, down_2.png ...
                String path = basePath + (i + 1) + ".png";
                if (Gdx.files.internal(path).exists()) {
                    Texture texture = new Texture(Gdx.files.internal(path));
                    frames[i] = new TextureRegion(texture);
                } else {
                    frames[i] = null;
                    System.out.println("Missing texture: " + path);
                }
            } catch (Exception e) {
                frames[i] = null;
            }
        }
        return frames;
    }

    // 镜像翻转辅助方法
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

    @Override
    public void update(float delta, List<Wall> walls) {
        if (!isAlive()) return;

        // 更新动画时间
        stateTime += delta;

        // 昼夜机制（保留原逻辑）
        updateDayNightCycle(delta);

        // 移动方向判断
        updateDirection();

        // 睁眼时速度正常，闭眼减半
        float currentSpeed = eyesOpen ? speed : speed * 0.5f;

        // 目标移动逻辑 (如果有特定目标点)
        if (targetPosition != null) {
            Vector2 direction = new Vector2(
                    targetPosition.x - position.x,
                    targetPosition.y - position.y
            );
            if (direction.len() > 1f) {
                direction.nor();
                velocity.set(direction.x * currentSpeed, direction.y * currentSpeed);
            } else {
                velocity.set(0, 0);
            }
        }

        // 调用父类更新（处理移动和碰撞）
        super.update(delta, walls);
    }

    private void updateDayNightCycle(float delta) {
        dayNightTimer += delta;
        if (dayNightTimer >= 5.0f) {
            eyesOpen = !eyesOpen;
            dayNightTimer = 0f;
            System.out.println("ZhuLong eyes: " + (eyesOpen ? "OPEN" : "CLOSED"));
        }
    }

    private void updateDirection() {
        if (velocity.len() > 0.1f) {
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

        Animation<TextureRegion> anim = walkAnimations.get(currentDirection);
        TextureRegion currentFrame = null;

        if (anim != null) {
            // 获取当前帧，looping=true
            currentFrame = anim.getKeyFrame(stateTime, true);
        }

        if (currentFrame != null) {
            // 设定绘制大小（建议设大一点，比如 64 或 80，以此体现它是 Boss）
            // 碰撞箱是 32，但绘制可以是 64，会有压迫感
            float drawWidth = 80f;
            float drawHeight = 80f;

            float offsetX = (bounds.width - drawWidth) / 2;
            float offsetY = (bounds.height - drawHeight) / 2;

            // 闭眼时变暗一点，作为视觉反馈（替代之前的变色方块）
            if (!eyesOpen) {
                batch.setColor(0.6f, 0.6f, 0.6f, 1f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }

            batch.draw(currentFrame, position.x + offsetX, position.y + offsetY, drawWidth, drawHeight);

            // 重置颜色
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    @Override
    public void attack() {
        // 简单的攻击反馈
        float damageMultiplier = eyesOpen ? 1.5f : 0.8f;
        System.out.println("ZhuLong attacks! Damage multiplier: " + damageMultiplier);
        if (targetPlayer != null && isInAttackRange()) {
            targetPlayer.takeDamage(attackDamage * damageMultiplier);
        }
    }

    @Override
    public void adjustDifficulty(int level) {
        this.maxHealth = 200 + (level * 40);
        this.health = this.maxHealth;
        this.attackDamage = 10 + (level * 3);
        this.speed = 40f + (level * 2f);
    }

    @Override
    protected void onDeath() {
        System.out.println("ZhuLong Defeated!");
    }

    // 资源清理
    public void dispose() {
        if (walkAnimations != null) {
            for (Animation<TextureRegion> anim : walkAnimations.values()) {
                Object[] frames = anim.getKeyFrames();
                for (Object frameObj : frames) {
                    if (frameObj instanceof TextureRegion) {
                        TextureRegion tr = (TextureRegion) frameObj;
                        if (tr.getTexture() != null) {
                            // 注意：多个Region可能共用一个Texture，这里简单dispose可能会导致问题
                            // 最好的方式是在 AssetManager 里管理，或者就不手动dispose texture
                            // 只要确保游戏结束时清理即可
                        }
                    }
                }
            }
        }
    }

    // Getter Setters
    public void setTargetPosition(Vector2 target) { this.targetPosition = target; }
    public boolean isEyesOpen() { return eyesOpen; }
    public void setEyesOpen(boolean open) { this.eyesOpen = open; }
    public int getCurrentForm() { return 0; } // 兼容旧接口，不再使用
}
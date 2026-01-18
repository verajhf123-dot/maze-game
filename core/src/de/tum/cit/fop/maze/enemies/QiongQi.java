package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.Wall;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class QiongQi extends Enemy {
    // 动画相关
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Map<Direction, Animation<TextureRegion>> walkAnimations;
    private Map<Direction, TextureRegion> idleFrames;

    private Direction currentDirection = Direction.DOWN;
    private float stateTime = 0f;
    private float animationSpeed = 0.15f;

    // 攻击相关
    private boolean isAttacking = false;
    private float attackCooldown = 1.2f;
    private float currentAttackCooldown = 0f;

    // 冲撞能力
    private boolean isCharging = false;
    private float chargeSpeed = 250f;
    private float normalSpeed;
    private float chargeCooldown = 8f;
    private float currentChargeCooldown = 0;
    private Vector2 chargeDirection;
    private float chargeDuration = 1.5f;
    private float chargeTimer = 0f;

    public QiongQi(float x, float y) {
        super(x, y, 30, 30);

        // 敌人数值
        this.normalSpeed = 70f;
        this.speed = normalSpeed;
        this.maxHealth = 120f;
        this.health = maxHealth;
        this.attackDamage = 15f;
        this.attackRange = 45f;
        this.detectionRange = 180f;

        // 初始化动画系统
        initializeAnimations();

        System.out.println("QiongQi initialized (3 frames per direction)");
    }

    private void initializeAnimations() {
        walkAnimations = new HashMap<>();
        idleFrames = new HashMap<>();

        try {
            // 加载每个方向的行走动画（3帧）
            TextureRegion[] downFrames = loadDirectionFrames("enemies/qiongqi/down_", 3);
            if (downFrames[0] != null) {
                walkAnimations.put(Direction.DOWN, new Animation<>(animationSpeed, downFrames));
                idleFrames.put(Direction.DOWN, downFrames[1]); // 使用第二帧作为待机
            }

            TextureRegion[] upFrames = loadDirectionFrames("enemies/qiongqi/up_", 3);
            if (upFrames[0] != null) {
                walkAnimations.put(Direction.UP, new Animation<>(animationSpeed, upFrames));
                idleFrames.put(Direction.UP, upFrames[1]);
            }

            TextureRegion[] leftFrames = loadDirectionFrames("enemies/qiongqi/left_", 3);
            if (leftFrames[0] != null) {
                walkAnimations.put(Direction.LEFT, new Animation<>(animationSpeed, leftFrames));
                idleFrames.put(Direction.LEFT, leftFrames[1]);
            }

            TextureRegion[] rightFrames = loadDirectionFrames("enemies/qiongqi/right_", 3);
            if (rightFrames[0] != null) {
                walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, rightFrames));
                idleFrames.put(Direction.RIGHT, rightFrames[1]);
            } else if (leftFrames[0] != null) {
                // 如果没有向右图片，镜像向左图片
                TextureRegion[] mirroredFrames = mirrorFrames(leftFrames);
                walkAnimations.put(Direction.RIGHT, new Animation<>(animationSpeed, mirroredFrames));
                idleFrames.put(Direction.RIGHT, mirroredFrames[1]);
            }

            System.out.println("QiongQi animations loaded successfully (3 frames per direction)");

        } catch (Exception e) {
            System.out.println("Error loading QiongQi animations: " + e.getMessage());
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
            Texture fallback = new Texture(Gdx.files.internal("enemies/qiongqi.png"));
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

            System.out.println("Using fallback texture for QiongQi");
        } catch (Exception e) {
            System.out.println("No fallback texture available for QiongQi");
        }
    }

    @Override
    public void update(float delta, List<Wall> walls) { // 增加 List<Wall> 参数
        super.update(delta, walls);

        // 更新动画计时器
        stateTime += delta;

        // 更新攻击冷却
        if (currentAttackCooldown > 0) {
            currentAttackCooldown -= delta;
        }

        // 更新冲撞冷却
        if (currentChargeCooldown > 0) {
            currentChargeCooldown -= delta;
        }

        // 更新冲撞状态
        if (isCharging) {
            chargeTimer -= delta;
            if (chargeTimer <= 0) {
                endCharge();
            } else {
                // 冲撞期间保持方向
                performCharge(delta);
                return;
            }
        }

        // 更新移动方向
        updateDirection();

        // 更新目标玩家
        if (targetPlayer != null) {
            Vector2 playerPos = targetPlayer.getPosition();
            Vector2 qiongqiPos = getPosition();
            float distance = qiongqiPos.dst(playerPos);

            // 检查是否在攻击范围内且可以攻击
            if (distance <= attackRange && currentAttackCooldown <= 0) {
                performMeleeAttack(targetPlayer);
                isAttacking = true;
                currentAttackCooldown = attackCooldown;
            }

            // 检查是否可以使用冲撞
            if (!isCharging && currentChargeCooldown <= 0 && distance < 100f) {
                if (Math.random() < 0.3f) { // 30%几率发动冲撞
                    startCharge(playerPos);
                }
            }
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

        boolean isMoving = velocity.len() > 0.1f || isCharging;

        TextureRegion currentFrame = null;

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
            // 固定大小，不随状态变化
            float drawWidth = 60f;  // 固定宽度
            float drawHeight = 60f; // 固定高度

            // 计算中心偏移
            float offsetX = (bounds.width - drawWidth) / 2;
            float offsetY = (bounds.height - drawHeight) / 2;

            // 充电时的视觉特效（颜色变化而不是大小变化）
            if (isCharging) {
                batch.setColor(1f, 0.8f, 0.8f, 1f);
                batch.draw(currentFrame,
                        position.x + offsetX,
                        position.y + offsetY,
                        drawWidth,  // 强制宽度
                        drawHeight  // 强制高度
                );
                batch.setColor(1f, 1f, 1f, 1f);
            } else {
                batch.draw(currentFrame,
                        position.x + offsetX,
                        position.y + offsetY,
                        drawWidth,  // 强制宽度
                        drawHeight  // 强制高度
                );
            }
        }
    }

    @Override
    public void attack() {
        if (targetPlayer != null) {
            performMeleeAttack(targetPlayer);
        }
    }

    private void performMeleeAttack(Player player) {
        if (player != null) {
            float distance = position.dst(player.getPosition());
            if (distance <= attackRange) {
                float finalDamage = attackDamage;

                if (Math.random() < 0.15f) {
                    finalDamage *= 1.8f;
                    System.out.println("QIONGQI CRITICAL HIT!");
                }

                player.takeDamage(finalDamage);

                System.out.println("[MELEE] QiongQi attacks player for " + finalDamage + " damage");

                if (currentBehavior != null) {
                    currentBehavior.onAttack();
                }
            }
        }
    }

    private void startCharge(Vector2 targetPos) {
        if (isCharging) return;

        Vector2 direction = new Vector2(targetPos.x - position.x, targetPos.y - position.y).nor();
        chargeDirection = direction;
        isCharging = true;
        chargeTimer = chargeDuration;
        speed = chargeSpeed;
        currentChargeCooldown = chargeCooldown;

        System.out.println("QiongQi starts charging!");
    }

    private void performCharge(float delta) {
        if (chargeDirection != null) {
            velocity.set(chargeDirection.x * speed, chargeDirection.y * speed);
            checkChargeCollision();
        }
    }

    private void checkChargeCollision() {
        if (targetPlayer != null) {
            float distance = position.dst(targetPlayer.getPosition());
            if (distance <= attackRange * 1.5f) {
                float chargeDamage = attackDamage * 1.5f;
                targetPlayer.takeDamage(chargeDamage);
                System.out.println("[CHARGE] QiongQi charges through player for " + chargeDamage + " damage");
            }
        }
    }

    private void endCharge() {
        isCharging = false;
        speed = normalSpeed;
        velocity.set(0, 0);
        System.out.println("QiongQi ends charging");
    }

    @Override
    public void adjustDifficulty(int level) {
        this.maxHealth = 80 + (level * 12);
        this.health = this.maxHealth;
        this.attackDamage = 12 + (level * 2.5f);
        this.speed = 70f + (level * 4f);
        this.normalSpeed = speed;
        this.chargeSpeed = 250f + (level * 10f);
        this.attackCooldown = Math.max(0.8f, 1.2f - (level * 0.06f));
        this.chargeCooldown = Math.max(4f, 8f - (level * 0.4f));

        System.out.println("QiongQi adjusted - HP=" + maxHealth +
                ", DMG=" + attackDamage + ", Charge Speed=" + chargeSpeed);
    }

    @Override
    protected void onDeath() {
        System.out.println("QiongQi has been defeated!");
    }

    // 添加缺失的方法
    public int getCurrentForm() {
        return 1;
    }

    public boolean hasEncounteredPlayer() {
        return targetPlayer != null;
    }

    public void resetEncounterState() {
        System.out.println("QiongQi encounter state reset");
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public void dispose() {
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

    public boolean isCharging() {
        return isCharging;
    }

    public float getChargeCooldown() {
        return currentChargeCooldown;
    }
}
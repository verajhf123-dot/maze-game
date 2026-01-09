package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.ai.AIBehavior;
import de.tum.cit.fop.maze.ai.AStarPathFinder;
import de.tum.cit.fop.maze.Player; // 添加这行导入
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;

public abstract class Enemy {
    public enum EnemyState {
        PATROL,      // 巡逻
        CHASE,       // 追击
        ATTACK,      // 攻击
        RETREAT,     // 撤退
        EVADE,       // 规避
        STUNNED      // 眩晕
    }
    protected Vector2 position;
    protected Vector2 velocity;
    protected Rectangle bounds;
    protected float speed;
    protected float health;
    protected float maxHealth;
    protected float attackDamage;
    protected float attackRange;
    protected float detectionRange;
    protected Texture texture;
    protected AIBehavior currentBehavior;
    protected AStarPathFinder pathFinder;
    protected List<Vector2> currentPath;
    protected int currentPathIndex;

    protected EnemyState currentState = EnemyState.PATROL;
    protected float stateTimer = 0f;
    protected float retreatHealthThreshold = 0.3f;
    protected float evadeCooldown = 3f;
    protected float currentEvadeCooldown = 0f;
    protected Player targetPlayer;

    protected float attackCooldown = 0.5f;
    protected float attackTimer = 0f;
    private static final float ATTACK_COOLDOWN_TIME = 0.8f;

    protected float pathfindingCooldown = 0.5f;
    protected float currentPathfindingCooldown = 0f;

    public Enemy(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        this.bounds = new Rectangle(x, y, width, height);
        this.currentPathIndex = 0;
    }

    public Texture getTexture() {return texture;}
    public Color getFallbackBodyColor(){
        return Color.RED;
    }

    protected Texture safeLoadTexture(String path) {
        if (Gdx.files.internal(path).exists()) {
            return new Texture(Gdx.files.internal(path));
        } else {
            return null;
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void clearPath() {
        currentPath = null;
        currentPathIndex = 0;
        velocity.set(0, 0);
    }

    public void setWalkableGrid(boolean[][] grid) {
    }

    public void attack(Player player) {
        if (player == null) return;

        if (attackTimer > 0f) return;

        float distance = position.dst(player.getPosition());
        if (distance > attackRange) return;

        System.out.println("[ATTACK] " + this.getClass().getSimpleName()
                + " attacks player for " + attackDamage + " damage");

        float finalDamage = attackDamage;

        if (Math.random() < 0.1f) {
            finalDamage *= 1.5f;
            System.out.println("ENEMY CRITICAL HIT!");
        }

        player.takeDamage(finalDamage);

        attackTimer = attackCooldown;

        if (currentBehavior != null) {
            currentBehavior.onAttack();
        }
    }

    public abstract void attack();

    public void setTargetPosition(Vector2 target) {
        findPathTo(target);
    }

    public void update(float delta) {
        if (!isAlive()) return;

        attackTimer = Math.max(0f, attackTimer - delta);
        currentPathfindingCooldown = Math.max(0f, currentPathfindingCooldown - delta);
        currentEvadeCooldown = Math.max(0f, currentEvadeCooldown - delta);
        stateTimer += delta;

        updateState(delta);

        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position.x, position.y);

        if (currentBehavior != null) {
            currentBehavior.update(delta);
        }

        if (currentPath != null && !currentPath.isEmpty()) {
            followPath(delta);
        }

        keepInBounds();
    }

    private void updateState(float delta) {
        if (targetPlayer == null) return;

        float distanceToPlayer = position.dst(targetPlayer.getPosition());
        float healthRatio = health / maxHealth;

        switch (currentState) {
            case PATROL:
                if (distanceToPlayer <= detectionRange) {
                    changeState(EnemyState.CHASE);
                }
                break;

            case CHASE:
                if (distanceToPlayer <= attackRange) {
                    changeState(EnemyState.ATTACK);
                } else if (distanceToPlayer > detectionRange * 1.5f) {
                    changeState(EnemyState.PATROL);
                }
                break;

            case ATTACK:
                if (distanceToPlayer > attackRange) {
                    changeState(EnemyState.CHASE);
                } else if (healthRatio < retreatHealthThreshold) {
                    changeState(EnemyState.RETREAT);
                }
                break;

            case RETREAT:
                if (stateTimer > 5f || distanceToPlayer > detectionRange * 2) {
                    changeState(EnemyState.PATROL);
                } else if (healthRatio > 0.5f && distanceToPlayer < attackRange) {
                    changeState(EnemyState.ATTACK);
                }
                break;

            case EVADE:
                if (stateTimer > 1f) {
                    changeState(previousState);
                }
                break;
        }

        if (currentEvadeCooldown <= 0 && distanceToPlayer < 50f && currentState != EnemyState.EVADE) {
            if (Math.random() < 0.3f) {
                changeState(EnemyState.EVADE);
                currentEvadeCooldown = evadeCooldown;
            }
        }
    }

    private EnemyState previousState;

    private void changeState(EnemyState newState) {
        if (currentState == newState) return;

        previousState = currentState;
        System.out.println(this.getClass().getSimpleName() +
                " state: " + currentState + " -> " + newState);
        currentState = newState;
        stateTimer = 0f;

        switch (newState) {
            case PATROL:
                setBehavior(new de.tum.cit.fop.maze.ai.PatrolBehavior(this));
                break;
            case CHASE:
            case ATTACK:
                setBehavior(new de.tum.cit.fop.maze.ai.AttackBehavior(this, targetPlayer));
                break;
            case RETREAT:
                setBehavior(new de.tum.cit.fop.maze.ai.RetreatBehavior(this));
                if (currentBehavior instanceof de.tum.cit.fop.maze.ai.RetreatBehavior) {
                    ((de.tum.cit.fop.maze.ai.RetreatBehavior) currentBehavior).setTargetPlayer(targetPlayer);
                }
                break;
            case EVADE:
                setBehavior(new de.tum.cit.fop.maze.ai.EvadeBehavior(this));
                if (currentBehavior instanceof de.tum.cit.fop.maze.ai.EvadeBehavior) {
                    ((de.tum.cit.fop.maze.ai.EvadeBehavior) currentBehavior).setTargetPlayer(targetPlayer);
                }
                break;
            case STUNNED:
                velocity.set(0, 0);
                break;
        }
    }

    private void followPath(float delta) {
        if (currentPathIndex >= currentPath.size()) {
            clearPath();
            return;
        }

        Vector2 target = currentPath.get(currentPathIndex);
        Vector2 direction = new Vector2(target.x - position.x, target.y - position.y);

        if (direction.len() < 5f) {
            currentPathIndex++;
        } else {
            direction.nor();
            velocity.set(direction.x * speed, direction.y * speed);
        }
    }

    private void keepInBounds() {
        if (position.x < 0) position.x = 0;
        if (position.y < 0) position.y = 0;
        if (position.x > 800 - bounds.width) position.x = 800 - bounds.width;
        if (position.y > 600 - bounds.height) position.y = 600 - bounds.height;
    }

    public void setPathFinder(AStarPathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    public void findPathTo(Vector2 target) {
        if (currentPathfindingCooldown > 0) return;

        if (pathFinder != null) {
            currentPath = pathFinder.findPath(position, target);
            currentPathIndex = 0;
            currentPathfindingCooldown = pathfindingCooldown;
        }
    }

    public void setBehavior(AIBehavior behavior) {
        this.currentBehavior = behavior;
    }

    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            onDeath();
        } else {
            if (Math.random() < 0.2f && currentState != EnemyState.EVADE) {
                changeState(EnemyState.EVADE);
            }
        }
    }

    public abstract void render(SpriteBatch batch);
    protected abstract void onDeath();

    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }
    public float getSpeed() { return speed; }
    public float getHealth() { return health; }
    public float getAttackRange() { return attackRange; }
    public float getDetectionRange() { return detectionRange; }
    public void setVelocity(float x, float y) { velocity.set(x, y); }

    public float getX() {
        return bounds.x;
    }

    public float getY() {
        return bounds.y;
    }

    public float getWidth() {
        return bounds.width;
    }

    public float getHeight() {
        return bounds.height;
    }

    public float getMaxHealth() {
        return maxHealth;
    }
    public float getAttackDamage() { return attackDamage; }

    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public EnemyState getCurrentState() {
        return currentState;
    }

    public boolean isInAttackRange() {
        if (targetPlayer == null) return false;
        float distance = position.dst(targetPlayer.getPosition());
        return distance <= attackRange;
    }

    public boolean isInDetectionRange() {
        if (targetPlayer == null) return false;
        float distance = position.dst(targetPlayer.getPosition());
        return distance <= detectionRange;
    }

    public float getHealthRatio() {
        return health / maxHealth;
    }

    public void adjustDifficulty(int level) {
        this.maxHealth = 50 + (level * 20);
        this.health = this.maxHealth;

        this.attackDamage = 5 + (level * 2);

        this.speed = 80f + (level * 5f);

        this.retreatHealthThreshold = Math.max(0.1f, 0.3f - (level * 0.02f));

        System.out.println(this.getClass().getSimpleName() +
                " adjusted - HP=" + maxHealth +
                ", DMG=" + attackDamage +
                ", Retreat at " + (retreatHealthThreshold * 100) + "%");
    }
}
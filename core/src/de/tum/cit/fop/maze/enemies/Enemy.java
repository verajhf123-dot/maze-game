package de.tum.cit.fop.maze.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.Player;
import de.tum.cit.fop.maze.Wall;
import de.tum.cit.fop.maze.ai.AIBehavior;
import de.tum.cit.fop.maze.ai.AStarPathFinder;

import java.util.List;

/**
 * Base class for all enemies in the game.
 * Handles movement, state management, attacks, and basic AI behaviors.
 */
public abstract class Enemy {

    public enum EnemyState {
        PATROL,
        CHASE,
        ATTACK,
        RETREAT,
        EVADE,
        STUNNED
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

    protected AIBehavior behavior;

    protected AStarPathFinder pathFinder;
    protected List<Vector2> path;
    protected int pathIndex;

    protected EnemyState state = EnemyState.PATROL;
    protected EnemyState prevState;

    protected float stateTime = 0f;

    protected float retreatHealthLimit = 0.3f;

    protected float evadeCooldown = 3f;
    protected float evadeCdLeft = 0f;

    protected float attackCooldown = 0.5f;
    protected float attackTimer = 0f;

    protected float pathCooldown = 0.5f;
    protected float pathCdLeft = 0f;

    protected float damageCooldown = 0f;

    protected Player player;

    protected float mapWidth = 2000f;
    protected float mapHeight = 2000f;

    public Enemy(float x, float y, float width, float height) {
        position = new Vector2(x, y);
        velocity = new Vector2();
        bounds = new Rectangle(x, y, width, height);
        pathIndex = 0;
    }

    // ------------------- Public Methods -------------------

    public Texture getTexture() {
        return texture;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }
    public float getSpeed() { return speed; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getAttackRange() { return attackRange; }
    public float getDetectionRange() { return detectionRange; }
    public float getHealthRatio() { return health / maxHealth; }

    public void setTargetPlayer(Player player) {
        this.player = player;
    }

    public void setPathFinder(AStarPathFinder finder) {
        this.pathFinder = finder;
    }

    public void setMapLimits(float w, float h) {
        mapWidth = w;
        mapHeight = h;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        bounds.setPosition(x, y);
    }

    public void setBehavior(AIBehavior behavior) {
        this.behavior = behavior;
    }

    /**
     * Set the current movement velocity of the enemy.
     * @param x horizontal speed
     * @param y vertical speed
     */
    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }

    /**
     * Get a copy of the current velocity vector.
     */
    public Vector2 getVelocity() {
        return new Vector2(velocity);
    }

    // ------------------- Game Loop Methods -------------------

    public void update(float delta, List<Wall> walls) {
        if (!isAlive()) return;

        damageCooldown = Math.max(0f, damageCooldown - delta);
        attackTimer = Math.max(0f, attackTimer - delta);
        pathCdLeft = Math.max(0f, pathCdLeft - delta);
        evadeCdLeft = Math.max(0f, evadeCdLeft - delta);

        stateTime += delta;

        updateState();

        if (behavior != null) {
            behavior.update(delta);
        }

        move(delta, walls);

        if (path != null && !path.isEmpty()) {
            followPath();
        }

        keepInBounds();
    }

    private void move(float delta, List<Wall> walls) {
        float oldX = position.x;
        position.x += velocity.x * delta;
        bounds.x = position.x;

        if (collides(walls)) {
            position.x = oldX;
            bounds.x = oldX;
        }

        float oldY = position.y;
        position.y += velocity.y * delta;
        bounds.y = position.y;

        if (collides(walls)) {
            position.y = oldY;
            bounds.y = oldY;
        }
    }

    private boolean collides(List<Wall> walls) {
        if (walls == null) return false;

        for (Wall wall : walls) {
            if (bounds.overlaps(wall.getBounds())) {
                return true;
            }
        }
        return false;
    }

    private void updateState() {
        if (player == null) return;

        float dist = position.dst(player.getPosition());
        float hpRatio = health / maxHealth;

        switch (state) {
            case PATROL:
                if (dist <= detectionRange) changeState(EnemyState.CHASE);
                break;

            case CHASE:
                if (dist <= attackRange) changeState(EnemyState.ATTACK);
                else if (dist > detectionRange * 1.5f) changeState(EnemyState.PATROL);
                break;

            case ATTACK:
                if (dist > attackRange) changeState(EnemyState.CHASE);
                else if (hpRatio < retreatHealthLimit) changeState(EnemyState.RETREAT);
                break;

            case RETREAT:
                if (stateTime > 5f || dist > detectionRange * 2f) changeState(EnemyState.PATROL);
                else if (hpRatio > 0.5f && dist < attackRange) changeState(EnemyState.ATTACK);
                break;

            case EVADE:
                if (stateTime > 1f) changeState(prevState);
                break;
        }

        // Random evade behavior
        if (evadeCdLeft <= 0f && dist < 50f && state != EnemyState.EVADE) {
            if (Math.random() < 0.3f) {
                changeState(EnemyState.EVADE);
                evadeCdLeft = evadeCooldown;
            }
        }
    }

    protected void changeState(EnemyState next) {
        if (state == next) return;

        prevState = state;
        state = next;
        stateTime = 0f;

        System.out.println(getClass().getSimpleName() +
                " state: " + prevState + " -> " + state);

        switch (state) {
            case PATROL:
                setBehavior(new de.tum.cit.fop.maze.ai.PatrolBehavior(this));
                break;
            case CHASE:
            case ATTACK:
                setBehavior(new de.tum.cit.fop.maze.ai.AttackBehavior(this, player));
                break;
            case RETREAT:
                var retreat = new de.tum.cit.fop.maze.ai.RetreatBehavior(this);
                retreat.setTargetPlayer(player);
                setBehavior(retreat);
                break;
            case EVADE:
                var evade = new de.tum.cit.fop.maze.ai.EvadeBehavior(this);
                evade.setTargetPlayer(player);
                setBehavior(evade);
                break;
            case STUNNED:
                velocity.set(0f, 0f);
                break;
        }
    }

    private void followPath() {
        if (pathIndex >= path.size()) {
            clearPath();
            return;
        }

        Vector2 target = path.get(pathIndex);
        Vector2 dir = new Vector2(target.x - position.x, target.y - position.y);

        if (dir.len() < 5f) {
            pathIndex++;
        } else {
            dir.nor();
            velocity.set(dir.x * speed, dir.y * speed);
        }
    }

    private void keepInBounds() {
        position.x = Math.max(0f, Math.min(position.x, mapWidth - bounds.width));
        position.y = Math.max(0f, Math.min(position.y, mapHeight - bounds.height));
    }

    public void findPathTo(Vector2 target) {
        if (pathCdLeft > 0f || pathFinder == null) return;

        path = pathFinder.findPath(position, target);
        pathIndex = 0;
        pathCdLeft = pathCooldown;
    }

    public void attack(Player player) {
        if (player == null || attackTimer > 0f) return;

        if (position.dst(player.getPosition()) > attackRange) return;

        float dmg = attackDamage;

        if (Math.random() < 0.1f) {
            dmg *= 1.5f;
            System.out.println("ENEMY CRITICAL HIT!");
        }

        player.takeDamage(dmg);
        attackTimer = attackCooldown;

        if (behavior != null) behavior.onAttack();
    }

    public void takeDamage(float damage) {
        if (damageCooldown > 0f) return;

        health -= damage;
        damageCooldown = 0.3f;

        if (health <= 0) onDeath();
        else if (Math.random() < 0.2f && state != EnemyState.EVADE) changeState(EnemyState.EVADE);
    }

    public void clearPath() {
        path = null;
        pathIndex = 0;
        velocity.set(0f, 0f);
    }

    public void adjustDifficulty(int level) {
        maxHealth = 30 + level * 15;
        health = maxHealth;
        attackDamage = 6 + level * 2.5f;
        speed = 90f + level * 2f;
        retreatHealthLimit = Math.max(0.1f, 0.3f - level * 0.02f);

        System.out.println(getClass().getSimpleName() +
                " adjusted - HP=" + maxHealth +
                ", DMG=" + attackDamage +
                ", retreat at " + (retreatHealthLimit * 100) + "%");
    }

    // ------------------- Abstract Methods -------------------
    public abstract void attack();
    public abstract void render(SpriteBatch batch);
    protected abstract void onDeath();
    // ------------------- Walkable Grid -------------------
    protected boolean[][] walkableGrid; // 可行走格子地图，用于 A* 或行为

    /**
     * Set the walkable grid for pathfinding.
     * @param grid 2D boolean array where true = walkable
     */
    public void setWalkableGrid(boolean[][] grid) {
        this.walkableGrid = grid;
    }

    /**
     * Get the walkable grid
     */
    public boolean[][] getWalkableGrid() {
        return walkableGrid;
    }
}
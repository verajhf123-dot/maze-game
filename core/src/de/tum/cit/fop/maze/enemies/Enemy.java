package de.tum.cit.fop.maze.enemies;

import de.tum.cit.fop.maze.Wall;
import java.util.List;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.ai.AIBehavior;
import de.tum.cit.fop.maze.ai.AStarPathFinder;
import de.tum.cit.fop.maze.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * Abstract base class for all enemy types.
 * Handles movement, AI behavior switching, pathfinding, combat,
 * state management, and interactions with the player and environment.
 */
public abstract class Enemy {

    /** Possible enemy states for AI behavior. */
    public enum EnemyState {
        PATROL,
        CHASE,
        ATTACK,
        RETREAT,
        EVADE,
        STUNNED
    }

    protected Vector2 position;          // Current world position
    protected Vector2 velocity;          // Current movement velocity
    protected Rectangle bounds;          // Collision rectangle
    protected float speed;               // Movement speed
    protected float health;              // Current health
    protected float maxHealth;           // Maximum health
    protected float attackDamage;        // Damage dealt per attack
    protected float attackRange;         // Attack range
    protected float detectionRange;      // Player detection range
    protected Texture texture;           // Enemy sprite texture
    protected AIBehavior currentBehavior; // Current active AI behavior
    protected AStarPathFinder pathFinder; // Pathfinding system
    protected List<Vector2> currentPath; // Current path to follow
    protected int currentPathIndex;      // Index in the path list

    protected EnemyState currentState = EnemyState.PATROL; // Current AI state
    protected float stateTimer = 0f;       // Timer for current state
    protected float retreatHealthThreshold = 0.3f; // Health ratio to retreat
    protected float evadeCooldown = 3f;    // Cooldown between evades
    protected float currentEvadeCooldown = 0f; // Remaining evade cooldown
    protected Player targetPlayer;         // Player this enemy targets

    protected float attackCooldown = 0.5f; // Time between attacks
    protected float attackTimer = 0f;      // Remaining attack cooldown

    protected float pathfindingCooldown = 0.5f; // Time between path updates
    protected float currentPathfindingCooldown = 0f;
    protected float damageCooldown = 0f;   // Invulnerability timer after taking damage

    protected float mapWidthLimit = 2000f;  // Map boundary limits
    protected float mapHeightLimit = 2000f;

    /**
     * Creates a new enemy at the given position and size.
     */
    public Enemy(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        this.bounds = new Rectangle(x, y, width, height);
        this.currentPathIndex = 0;
    }

    /** Returns the enemy's texture for rendering. */
    public Texture getTexture() { return texture; }

    /** Safely loads a texture, returns null if the file does not exist. */
    protected Texture safeLoadTexture(String path) {
        if (Gdx.files.internal(path).exists()) {
            return new Texture(Gdx.files.internal(path));
        } else {
            return null;
        }
    }

    /** Checks if the enemy is alive. */
    public boolean isAlive() {
        return health > 0;
    }

    /** Clears the current path and stops movement. */
    public void clearPath() {
        currentPath = null;
        currentPathIndex = 0;
        velocity.set(0, 0);
    }

    /** Placeholder to set walkable grid for pathfinding. */
    public void setWalkableGrid(boolean[][] grid) {
    }

    /**
     * Performs an attack on the given player if in range
     * and attack cooldown allows it.
     */
    public void attack(Player player) {
        if (player == null) return;
        if (attackTimer > 0f) return;

        float distance = position.dst(player.getPosition());
        if (distance > attackRange) return;

        System.out.println("[ATTACK] " + this.getClass().getSimpleName()
                + " attacks player for " + attackDamage + " damage");

        float finalDamage = attackDamage;

        if (Math.random() < 0.1f) {
            finalDamage *= 1.5f; // Critical hit
            System.out.println("ENEMY CRITICAL HIT!");
        }

        player.takeDamage(finalDamage);

        attackTimer = attackCooldown;

        if (currentBehavior != null) {
            currentBehavior.onAttack();
        }
    }

    /** Abstract attack method for subclasses to implement. */
    public abstract void attack();

    /** Sets a target position and calculates a path to it. */
    public void setTargetPosition(Vector2 target) {
        findPathTo(target);
    }

    /**
     * Updates enemy logic, including state, AI, movement,
     * collisions, path following, and keeping in bounds.
     */
    public void update(float delta, List<Wall> walls) {
        if (!isAlive()) return;

        damageCooldown = Math.max(0f, damageCooldown - delta);
        attackTimer = Math.max(0f, attackTimer - delta);
        currentPathfindingCooldown = Math.max(0f, currentPathfindingCooldown - delta);
        currentEvadeCooldown = Math.max(0f, currentEvadeCooldown - delta);
        stateTimer += delta;

        updateState(delta);

        if (currentBehavior != null) {
            currentBehavior.update(delta);
        }

        // Move and handle collisions with walls
        float oldX = position.x;
        position.x += velocity.x * delta;
        bounds.x = position.x;
        boolean collisionX = false;
        if (walls != null) {
            for (Wall wall : walls) {
                if (bounds.overlaps(wall.getBounds())) {
                    collisionX = true;
                    break;
                }
            }
        }
        if (collisionX) {
            position.x = oldX;
            bounds.x = oldX;
        }

        float oldY = position.y;
        position.y += velocity.y * delta;
        bounds.y = position.y;
        boolean collisionY = false;
        if (walls != null) {
            for (Wall wall : walls) {
                if (bounds.overlaps(wall.getBounds())) {
                    collisionY = true;
                    break;
                }
            }
        }
        if (collisionY) {
            position.y = oldY;
            bounds.y = oldY;
        }

        // Follow path if exists
        if (currentPath != null && !currentPath.isEmpty()) {
            followPath(delta);
        }

        // Keep enemy inside map bounds
        keepInBounds();
    }

    /** Updates AI state machine based on distance, health, and cooldowns. */
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

        // Chance to evade if close to player and cooldown finished
        if (currentEvadeCooldown <= 0 && distanceToPlayer < 50f && currentState != EnemyState.EVADE) {
            if (Math.random() < 0.3f) {
                changeState(EnemyState.EVADE);
                currentEvadeCooldown = evadeCooldown;
            }
        }
    }

    private EnemyState previousState;

    /** Changes the enemy's AI state and sets corresponding behavior. */
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

    /** Moves along the current path using velocity. */
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

    /** Prevents enemy from leaving map boundaries. */
    private void keepInBounds() {
        if (position.x < 0) position.x = 0;
        if (position.y < 0) position.y = 0;
        if (position.x > mapWidthLimit - bounds.width) position.x = mapWidthLimit - bounds.width;
        if (position.y > mapHeightLimit - bounds.height) position.y = mapHeightLimit - bounds.height;
    }

    /** Sets the pathfinder for this enemy. */
    public void setPathFinder(AStarPathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    /** Finds a path to the target position. */
    public void findPathTo(Vector2 target) {
        if (currentPathfindingCooldown > 0) return;
        if (pathFinder != null) {
            currentPath = pathFinder.findPath(position, target);
            currentPathIndex = 0;
            currentPathfindingCooldown = pathfindingCooldown;
        }
    }

    /** Sets the current AI behavior. */
    public void setBehavior(AIBehavior behavior) {
        this.currentBehavior = behavior;
    }

    /** Applies damage to the enemy and handles death or evasion. */
    public void takeDamage(float damage) {
        if (damageCooldown > 0) return;
        health -= damage;
        damageCooldown = 0.3f;

        if (health <= 0) {
            onDeath();
        } else {
            if (Math.random() < 0.2f && currentState != EnemyState.EVADE) {
                changeState(EnemyState.EVADE);
            }
        }
    }

    /** Abstract render method to draw the enemy. */
    public abstract void render(SpriteBatch batch);

    /** Abstract method for handling death. */
    protected abstract void onDeath();

    // ----- Getters and setters for position, stats, and target -----
    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }
    public float getSpeed() { return speed; }
    public float getHealth() { return health; }
    public float getAttackRange() { return attackRange; }
    public float getDetectionRange() { return detectionRange; }
    public void setVelocity(float x, float y) { velocity.set(x, y); }
    public float getX() { return bounds.x; }
    public float getY() { return bounds.y; }
    public float getWidth() { return bounds.width; }
    public float getHeight() { return bounds.height; }
    public float getMaxHealth() { return maxHealth; }
    public float getAttackDamage() { return attackDamage; }
    public void setTargetPlayer(Player player) { this.targetPlayer = player; }
    public Player getTargetPlayer() { return targetPlayer; }
    public EnemyState getCurrentState() { return currentState; }
    public boolean isInAttackRange() { return targetPlayer != null && position.dst(targetPlayer.getPosition()) <= attackRange; }
    public boolean isInDetectionRange() { return targetPlayer != null && position.dst(targetPlayer.getPosition()) <= detectionRange; }
    public float getHealthRatio() { return health / maxHealth; }

    /** Adjusts enemy stats based on difficulty level. */
    public void adjustDifficulty(int level) {
        this.maxHealth = 30 + (level * 15);
        this.health = this.maxHealth;
        this.attackDamage = 6 + (level * 2.5f);
        this.speed = 90f + (level * 2f);
        this.retreatHealthThreshold = Math.max(0.1f, 0.3f - (level * 0.02f));

        System.out.println(this.getClass().getSimpleName() +
                " adjusted - HP=" + maxHealth +
                ", DMG=" + attackDamage +
                ", Retreat at " + (retreatHealthThreshold * 100) + "%");
    }

    /** Sets the enemy position and updates bounds. */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.bounds.setPosition(x, y);
    }

    /** Sets map boundaries for this enemy. */
    public void setMapLimits(float width, float height) {
        this.mapWidthLimit = width;
        this.mapHeightLimit = height;
    }
}
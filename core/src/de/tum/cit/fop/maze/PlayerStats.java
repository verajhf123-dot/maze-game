package de.tum.cit.fop.maze;

import de.tum.cit.fop.maze.progression.ExperienceSystem;
import de.tum.cit.fop.maze.progression.SkillTree;
import de.tum.cit.fop.maze.progression.SkillManager;

/**
 * Stores all player-related stats.
 */

public class PlayerStats {

    public static final String DAMAGE_TYPE_PHYSICAL = "physical";
    public static final String DAMAGE_TYPE_FIRE = "fire";
    public static final String DAMAGE_TYPE_POISON = "poison";

    private int maxHealth = 100;
    private int health = 100;
    private int score = 0;
    private boolean hasExitKey = false;
    private int bonusKeys = 0;

    private ExperienceSystem expSystem;
    private SkillTree skillTree;
    private SkillManager skillManager;

    private boolean canDoubleJump = false;
    private boolean canDash = false;
    private boolean fireResistance = false;
    private boolean poisonResistance = false;
    private float criticalChance = 0.0f;
    private float dodgeChance = 0.0f;

    private int baseMaxHealth = 100;
    private Player player;

    /**
     * Creates default stats and initializes the progression systems.
     * Stats are updated once based on the starting level/skills.
     */
    public PlayerStats() {
        expSystem = new ExperienceSystem();
        skillTree = new SkillTree(expSystem);
        skillManager = null;
        updateStatsFromLevel();
    }

    /**
     * Connects this stats object to a specific Player and creates the SkillManager.
     * Call this after the Player is created.
     *
     * @param player the player entity that uses these stats
     */

    public void setPlayer(Player player) {
        this.player = player;
        this.skillManager = new SkillManager(player, this);
        updateStatsFromLevel();
    }

    public SkillManager getSkillManager() { return skillManager; }
    public SkillTree getSkillTree() { return skillTree; }

    /**
     * Recalculates stats that depend on level and unlocked skills.
     * This updates maxHealth, resistances, and chance values, and clamps current health.
     */

    private void updateStatsFromLevel() {
        float skillHealthBonus = 0;
        if (skillTree != null) {
            skillHealthBonus = skillTree.getTotalHealthBonus();
        }

        int newMaxHealth = baseMaxHealth + (int)skillHealthBonus;

        if (newMaxHealth != maxHealth) {
            maxHealth = newMaxHealth;
        } else {
            maxHealth = newMaxHealth;
        }

        if (skillTree != null) {
            canDoubleJump = skillTree.hasDoubleJump();
            canDash = skillTree.hasDash();
            fireResistance = skillTree.hasFireResistance();
            poisonResistance = skillTree.hasPoisonResistance();
            criticalChance = skillTree.getTotalCritChance();
            dodgeChance = skillTree.getTotalDodgeChance();
        }

        if (health > maxHealth) {
            health = maxHealth;
        }
    }

/**
 * Calculates total defense bonus from level + skills.
 */
    private float calculateDefenseBonus() {
        float levelDefense = expSystem.getDefenseBonus();
        float skillDefense = skillTree != null ? skillTree.getTotalDefenseBonus() : 0;
        return levelDefense + skillDefense;
    }

    /**
     * Applies damage to the player, including resistances, defense, shield and trap resistance.
     * Can also dodge if the dodge ability is active and the random check succeeds.
     *
     * @param damage raw incoming damage
     * @param damageType type of damage (physical/fire/poison)
     */

    public void takeDamage(float damage, String damageType) {
        if (skillManager != null && skillManager.hasSpecialAbility("dodge") && Math.random() < dodgeChance) {
            System.out.println("Dodged!");
            return;
        }

        float finalDamage = damage;
        if (damageType.equals(DAMAGE_TYPE_FIRE) && fireResistance) finalDamage *= 0.5f;
        if (damageType.equals(DAMAGE_TYPE_POISON) && poisonResistance) finalDamage *= 0.5f;

        finalDamage -= calculateDefenseBonus() * 0.5f;
        if (finalDamage < 1) finalDamage = 1;

        if (skillTree != null && skillTree.hasShieldActive()) {
            finalDamage = skillTree.applyShield(finalDamage);
        }

        if (skillManager != null) {
            finalDamage = skillManager.applyTrapResistance(finalDamage);
        }

        if (finalDamage > 0) {
            health -= (int) finalDamage;
            if (health < 0) health = 0;
        }
    }
    /**
     * Convenience overload: treats the damage as physical damage.
     *
     * @param damage incoming damage amount
     */

    public void takeDamage(int damage) { takeDamage((float)damage, DAMAGE_TYPE_PHYSICAL); }
    /**
     * Calculates the player's current attack damage based on base damage and skill bonuses.
     *
     * @return attack damage value used for hits/projectiles
     */
    public float getActualAttackDamage() {
        float baseDamage = 10f;
        float skillBonus = skillTree != null ? skillTree.getTotalAttackBonus() : 0;
        float totalDamage = baseDamage + skillBonus;

        if (skillManager != null) {
            totalDamage = skillManager.applySkillBonusesToDamage(totalDamage);
        }
        return totalDamage;
    }

    public void applySkillEffects() { updateStatsFromLevel(); }

    public void gainExpFromKill(String enemyType) {
        int amount = ExperienceSystem.getExpForEnemy(enemyType);
        expSystem.gainExp(amount);
    }

    /**
     * Tries to unlock a skill and refreshes stats if unlocking succeeded.
     *
     * @param skillId skill identifier
     * @return true if the skill was unlocked
     */

    public boolean unlockSkill(String skillId) {
        if (skillTree == null) return false;
        boolean s = skillTree.unlockSkill(skillId);
        if (s) applySkillEffects();
        return s;
    }
    /**
     * Heals the player and clamps health to maxHealth.
     * Skill bonuses may increase healing.
     *
     * @param value heal amount
     */
    public void heal(int value) {
        if (skillManager != null) {
            float v = skillManager.applySkillBonusesToHealing(value);
            health += (int)v;
        } else {
            health += value;
        }
        if (health > maxHealth) health = maxHealth;
    }

    /**
     * Resets exp and skill tree progression, then restores health to max.
     */

    public void resetProgression() {
        expSystem.setCurrentExp(0);
        if (skillTree != null) skillTree.reset();
        updateStatsFromLevel();
        health = maxHealth;
    }

    /**
     * Updates time-based systems (skills, cooldowns) and refreshes derived stats.
     *
     * @param delta time since last frame
     */

    public void update(float delta) {
        if (skillTree != null) skillTree.update(delta);
        if (skillManager != null) skillManager.update(delta);
        updateStatsFromLevel();
    }

    public int getScore() { return score; }
    public void addScore(int amount) { this.score += amount; }
    public void setScore(int score) { this.score = score; }
    public void collectBonusKey() { this.bonusKeys++; addScore(500); }
    public void collectExitKey() { this.hasExitKey = true; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isDead() { return health <= 0; }
    public boolean hasKey() { return hasExitKey; }
    public void useKey() { if (hasExitKey) { hasExitKey = false; addScore(1000); } }
    public void setHasKey(boolean hasKey) { this.hasExitKey = hasKey; }
    public int getBonusKey() { return bonusKeys; }
    public void setBonusKey(int bonusKey) { this.bonusKeys = bonusKey; }
    public ExperienceSystem getExpSystem() { return expSystem; }
    public boolean canDoubleJump() { return canDoubleJump; }
    public boolean canDash() { return canDash; }
    public boolean hasFireResistance() { return fireResistance; }
    public boolean hasPoisonResistance() { return poisonResistance; }
    public float getCriticalChance() { return criticalChance; }
    public float getDodgeChance() { return dodgeChance; }
    public int getAvailableSkillPoints() { return expSystem.getSkillPoints(); }
    public void setCurrentHealth(int health) { this.health = health; if (this.health > maxHealth) this.health = maxHealth; }
}
package de.tum.cit.fop.maze;

import de.tum.cit.fop.maze.progression.ExperienceSystem;
import de.tum.cit.fop.maze.progression.SkillTree;
import de.tum.cit.fop.maze.progression.SkillManager;

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
    private float baseSpeedMultiplier = 1.0f;
    private float baseAttackMultiplier = 1.0f;
    private float baseDefenseMultiplier = 1.0f;

    // Reference to player for skill effects
    private Player player;

    public PlayerStats() {
        expSystem = new ExperienceSystem();
        skillTree = new SkillTree(expSystem);

        // SkillManager will be initialized later with setPlayer()
        skillManager = null;

        expSystem.addListener(new ExperienceSystem.ExpListener() {
            @Override
            public void onExpGained(int amount, int total) {
                System.out.println("Gained exp: " + amount + ", total: " + total);
            }

            @Override
            public void onLevelUp(int newLevel, int skillPoints) {
                System.out.println("=== LEVEL UP! ===");
                System.out.println("You are now level " + newLevel);
                System.out.println("You have " + skillPoints + " skill point(s) available!");
                System.out.println("Press T to open Skill Tree");

                // Heal on level up
                if (player != null) {
                    int healAmount = getMaxHealth() / 4;
                    heal(healAmount);
                    System.out.println("Healed " + healAmount + " HP from level up!");
                }

                // Update stats from new level
                updateStatsFromLevel();
            }

            @Override
            public void onSkillPointsChanged(int points) {
                System.out.println("Skill points now: " + points);
            }
        });

        // Initial stat calculation
        updateStatsFromLevel();
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.skillManager = new SkillManager(player, this);
        updateStatsFromLevel();
        System.out.println("Player and SkillManager initialized");
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public SkillTree getSkillTree() {
        return skillTree;
    }

    private void updateStatsFromLevel() {
        // Calculate health from level and skills
        int levelHealthBonus = expSystem.getHealthBonus();
        float skillHealthBonus = 0;
        if (skillTree != null) {
            skillHealthBonus = skillTree.getTotalHealthBonus();
        }

        maxHealth = baseMaxHealth + levelHealthBonus + (int)skillHealthBonus;

        // Update special abilities from skill tree
        if (skillTree != null) {
            canDoubleJump = skillTree.hasDoubleJump();
            canDash = skillTree.hasDash();
            fireResistance = skillTree.hasFireResistance();
            poisonResistance = skillTree.hasPoisonResistance();
        }

        // Update other stats
        criticalChance = skillTree != null ? skillTree.getTotalCritChance() : 0.0f;
        dodgeChance = skillTree != null ? skillTree.getTotalDodgeChance() : 0.0f;

        // Ensure health doesn't exceed new max
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    private float calculateDefenseBonus() {
        float levelDefense = expSystem.getDefenseBonus();
        float skillDefense = skillTree != null ? skillTree.getTotalDefenseBonus() : 0;
        return levelDefense + skillDefense;
    }

    public void takeDamage(float damage, String damageType) {
        // Check for dodge
        if (skillManager != null && skillManager.hasSpecialAbility("dodge") &&
                Math.random() < dodgeChance) {
            System.out.println("Dodged attack!");
            return;
        }

        // Apply damage type resistances
        float finalDamage = damage;

        if (damageType.equals(DAMAGE_TYPE_FIRE) && fireResistance) {
            finalDamage *= 0.5f;
            System.out.println("Fire resistance reduced damage by 50%");
        }

        if (damageType.equals(DAMAGE_TYPE_POISON) && poisonResistance) {
            finalDamage *= 0.5f;
            System.out.println("Poison resistance reduced damage by 50%");
        }

        // Apply defense bonus
        float defenseBonus = calculateDefenseBonus();
        finalDamage -= defenseBonus * 0.5f;

        // Minimum damage
        if (finalDamage < 1) finalDamage = 1;

        // Apply shield if available
        if (skillManager != null) {
            finalDamage = skillManager.applyTrapResistance(finalDamage);
        }

        health -= (int) finalDamage;
        if (health < 0) health = 0;

        System.out.println("Took " + (int)finalDamage + " " + damageType +
                " damage, health remaining: " + health);
    }

    public void takeDamage(int damage) {
        takeDamage((float)damage, DAMAGE_TYPE_PHYSICAL);
    }

    public float getActualAttackDamage() {
        float baseDamage = 10f;
        float levelBonus = expSystem.getAttackBonus();
        float skillBonus = skillTree != null ? skillTree.getTotalAttackBonus() : 0;

        float totalDamage = baseDamage + levelBonus + skillBonus;

        if (skillManager != null) {
            totalDamage = skillManager.applySkillBonusesToDamage(totalDamage);
        }

        return totalDamage;
    }

    public void applySkillEffects() {
        if (skillTree == null) return;

        // Apply skill tree bonuses
        float healthBonus = skillTree.getTotalHealthBonus();
        float speedBonus = skillTree.getTotalSpeedBonus();
        float attackBonus = skillTree.getTotalAttackBonus();

        maxHealth = baseMaxHealth + (int)healthBonus;

        // Update abilities
        canDoubleJump = skillTree.hasDoubleJump();
        canDash = skillTree.hasDash();
        fireResistance = skillTree.hasFireResistance();
        poisonResistance = skillTree.hasPoisonResistance();

        // Ensure health is within bounds
        if (health > maxHealth) {
            health = maxHealth;
        }

        System.out.println("Skill effects applied: HP +" + healthBonus +
                ", Speed +" + (speedBonus * 100) + "%, " +
                "ATK +" + attackBonus);
    }

    public void gainExpFromKill(String enemyType) {
        int exp = ExperienceSystem.getExpForEnemy(enemyType);
        expSystem.gainExp(exp);
    }

    public boolean unlockSkill(String skillId) {
        if (skillTree == null) return false;

        boolean success = skillTree.unlockSkill(skillId);
        if (success) {
            applySkillEffects();
        }
        return success;
    }

    public boolean isCriticalHit() {
        return Math.random() < criticalChance;
    }

    public boolean dodgeAttack() {
        return Math.random() < dodgeChance;
    }

    public void heal(int value) {
        if (skillManager != null) {
            float actualHealing = skillManager.applySkillBonusesToHealing(value);
            health += (int)actualHealing;
        } else {
            health += value;
        }

        if (health > maxHealth) {
            health = maxHealth;
        }
        System.out.println("Healed " + value + " HP, current health: " + health);
    }

    public void resetProgression() {
        expSystem.reset();
        if (skillTree != null) {
            skillTree.reset();
        }
        updateStatsFromLevel();
        health = maxHealth;
    }

    // Getters and Setters
    public int getScore() { return score; }
    public void addScore(int amount) { this.score += amount; }
    public void setScore(int score) { this.score = score; }

    public void collectBonusKey() {
        this.bonusKeys++;
        System.out.println("Bonus Key! Total: " + bonusKeys);
        addScore(500);
    }

    public void collectExitKey() {
        this.hasExitKey = true;
        System.out.println("EXIT KEY FOUND! Go to the door!");
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean hasKey() {
        return hasExitKey;
    }


    public void useKey() {
        if (hasExitKey) {
            hasExitKey = false;
            addScore(1000);
            System.out.println("Key has been used!");
        }
    }


    public void setHasKey(boolean hasKey) {
        this.hasExitKey = hasKey;
        if (hasKey) {
            System.out.println("CHEAT: Key added via Console!");
        }
    }


    public int getBonusKey() { return bonusKeys; }
    public void setBonusKey(int bonusKey) { this.bonusKeys = bonusKey; }

    public ExperienceSystem getExpSystem() { return expSystem; }

    public boolean canDoubleJump() { return canDoubleJump; }
    public boolean canDash() { return canDash; }
    public boolean hasFireResistance() { return fireResistance; }
    public boolean hasPoisonResistance() { return poisonResistance; }

    public float getCriticalChance() { return criticalChance; }
    public float getDodgeChance() { return dodgeChance; }

    public int getAvailableSkillPoints() {
        return expSystem.getSkillPoints();
    }

    public void update(float delta) {
        // Update experience system if needed
        if (expSystem != null) {
            // Experience system updates automatically on gainExp calls
        }

        // Update skill tree cooldowns
        if (skillTree != null) {
            skillTree.update(delta);
        }

        // Update skill manager
        if (skillManager != null) {
            skillManager.update(delta);
        }
    }

    public void setCurrentHealth(int health) {
        this.health = health;
        // 简单的安全检查
        if (this.health > maxHealth) this.health = maxHealth;
    }


}
